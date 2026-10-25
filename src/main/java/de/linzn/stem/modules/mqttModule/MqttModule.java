/*
 * Copyright (c) 2026 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.stem.modules.mqttModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.pluginModule.PluginModule;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * MQTT connection of STEM.
 * <ul>
 *     <li>Subscriptions are remembered and restored after every reconnect (Paho does not do that with clean sessions).</li>
 *     <li>If the broker is unreachable at startup, the connection is retried until it succeeds.</li>
 *     <li>Several listeners can subscribe to the same topic; each listener is tied to an owner plugin.</li>
 *     <li>Messages are handed to listeners on a separate, ordered dispatch thread, so slow listeners never block
 *     the Paho network thread and a failing listener can not drop the connection.</li>
 * </ul>
 */
public class MqttModule extends AbstractModule {
    private static final int DEFAULT_QOS = 1;
    private static final int RETRY_SECONDS = 30;

    /* topic filter -> listeners */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Subscription>> subscriptions = new ConcurrentHashMap<>();
    private final ExecutorService dispatcher = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("STEM-MQTT-Dispatch").factory());
    private final Object connectLock = new Object();
    private FileConfiguration fileConfiguration;
    private String broker;
    private String clientId;
    private String user;
    private String password;
    private volatile MqttClient mqttClient;
    private volatile MqttConnectOptions connectOptions;
    private volatile boolean shuttingDown = false;

    public MqttModule(STEMApp stemApp) {
        super(stemApp);
        this.initConfig();
        this.createClient();
    }

    private void createClient() {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqttClient = new MqttClient(broker, clientId, persistence);
        } catch (MqttException | IllegalArgumentException e) {
            STEMApp.LOGGER.ERROR("Invalid MQTT client configuration for broker " + broker);
            STEMApp.LOGGER.ERROR(e);
            return;
        }
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(user);
        connOpts.setPassword(password.toCharArray());
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(true);
        connOpts.setMaxReconnectDelay(RETRY_SECONDS * 1000);
        connOpts.setConnectionTimeout(10);
        connOpts.setKeepAliveInterval(30);
        this.connectOptions = connOpts;
        mqttClient.setCallback(new ConnectionCallback());
        this.connect();
    }

    /**
     * Connect to the broker. On failure a retry is scheduled. Paho's automatic reconnect only works
     * after the first successful connection, so the initial connection is retried here.
     */
    private void connect() {
        MqttClient client = this.mqttClient;
        if (client == null || this.shuttingDown) {
            return;
        }
        synchronized (this.connectLock) {
            if (client.isConnected()) {
                return;
            }
            try {
                STEMApp.LOGGER.INFO("Connecting to IOBroker " + broker + "...");
                client.connect(this.connectOptions);
                STEMApp.LOGGER.INFO("Connection to IOBroker is valid!");
                MqttMessage mqttMessage = new MqttMessage("Hello".getBytes(StandardCharsets.UTF_8));
                mqttMessage.setQos(2);
                client.publish("stem/test", mqttMessage);
            } catch (MqttException e) {
                STEMApp.LOGGER.ERROR("Connection to IOBroker " + broker + " failed. Retry in " + RETRY_SECONDS + " seconds");
                STEMApp.LOGGER.ERROR(e);
                this.getStemApp().getScheduler().runTaskLater(this.getModulePlugin(), this::connect, RETRY_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_mqtt.yml"));
        this.broker = this.fileConfiguration.getString("broker", "tcp://10.50.0.1:1883");
        this.clientId = this.fileConfiguration.getString("clientId", "STEM");
        this.user = this.fileConfiguration.getString("user", "testuser");
        this.password = this.fileConfiguration.getString("password", "GeheimesPW");
        this.fileConfiguration.save();
    }

    @Override
    public void onShutdown() {
        this.shuttingDown = true;
        MqttClient client = this.mqttClient;
        if (client != null) {
            try {
                if (client.isConnected()) {
                    MqttMessage mqttMessage = new MqttMessage("Bye".getBytes(StandardCharsets.UTF_8));
                    mqttMessage.setQos(1);
                    client.publish("stem/test", mqttMessage);
                    STEMApp.LOGGER.INFO("Disconnecting from IOBroker...");
                    client.disconnect(2000);
                    STEMApp.LOGGER.INFO("Disconnected from IOBroker!");
                }
            } catch (MqttException e) {
                STEMApp.LOGGER.ERROR(e);
            }
            try {
                client.close();
            } catch (MqttException e) {
                STEMApp.LOGGER.ERROR(e);
            }
        }
        this.subscriptions.clear();
        this.dispatcher.shutdownNow();
    }

    public boolean isConnected() {
        MqttClient client = this.mqttClient;
        return client != null && client.isConnected();
    }

    public boolean publish(String topic, MqttMessage mqttMessage) {
        MqttClient client = this.mqttClient;
        if (client == null || !client.isConnected()) {
            STEMApp.LOGGER.WARNING("MQTT not connected. Message to " + topic + " dropped");
            return false;
        }
        try {
            client.publish(topic, mqttMessage);
            return true;
        } catch (MqttException e) {
            STEMApp.LOGGER.ERROR(e);
            return false;
        }
    }

    /**
     * Subscribe to a topic. The owner plugin is detected automatically.
     * The subscription survives reconnects. If the broker is not connected yet, the topic is subscribed
     * as soon as the connection is established.
     *
     * @param topic                topic filter
     * @param iMqttMessageListener listener
     * @return true if the subscription is registered
     */
    public boolean subscribe(String topic, IMqttMessageListener iMqttMessageListener) {
        return this.subscribe(topic, iMqttMessageListener, PluginModule.findOwner(iMqttMessageListener.getClass()));
    }

    /**
     * Subscribe to a topic for a plugin.
     *
     * @param topic                topic filter
     * @param iMqttMessageListener listener
     * @param owner                owning plugin, may be null for core listeners
     * @return true if the subscription is registered
     */
    public boolean subscribe(String topic, IMqttMessageListener iMqttMessageListener, STEMPlugin owner) {
        if (topic == null || topic.isEmpty() || iMqttMessageListener == null) {
            return false;
        }
        boolean[] firstForTopic = {false};
        this.subscriptions.compute(topic, (_, list) -> {
            if (list == null) {
                list = new CopyOnWriteArrayList<>();
                firstForTopic[0] = true;
            }
            list.add(new Subscription(iMqttMessageListener, owner));
            return list;
        });
        if (firstForTopic[0]) {
            return this.subscribeAtBroker(topic);
        }
        return true;
    }

    private boolean subscribeAtBroker(String topic) {
        MqttClient client = this.mqttClient;
        if (client == null) {
            return false;
        }
        if (!client.isConnected()) {
            /* restored in connectComplete */
            return true;
        }
        try {
            client.subscribe(topic, DEFAULT_QOS, new TopicDispatcher(topic));
            return true;
        } catch (MqttException e) {
            STEMApp.LOGGER.ERROR(e);
            return false;
        }
    }

    /**
     * Remove a single listener from a topic
     *
     * @param topic                topic filter
     * @param iMqttMessageListener listener to remove
     */
    public void unsubscribe(String topic, IMqttMessageListener iMqttMessageListener) {
        CopyOnWriteArrayList<Subscription> list = this.subscriptions.get(topic);
        if (list == null) {
            return;
        }
        list.removeIf(subscription -> subscription.listener == iMqttMessageListener);
        this.removeTopicIfEmpty(topic);
    }

    /**
     * Remove all listeners of a topic
     *
     * @param topic topic filter
     */
    public void unsubscribe(String topic) {
        if (this.subscriptions.remove(topic) != null) {
            this.unsubscribeAtBroker(topic);
        }
    }

    /**
     * Remove all subscriptions of a plugin
     *
     * @param owner plugin to remove the subscriptions for
     */
    public void unsubscribeAll(STEMPlugin owner) {
        if (owner == null) {
            return;
        }
        for (Map.Entry<String, CopyOnWriteArrayList<Subscription>> entry : this.subscriptions.entrySet()) {
            if (entry.getValue().removeIf(subscription -> subscription.owner == owner)) {
                STEMApp.LOGGER.DEBUG("MQTT unsubscribe " + entry.getKey() + " from " + owner.getPluginName());
                this.removeTopicIfEmpty(entry.getKey());
            }
        }
    }

    private void removeTopicIfEmpty(String topic) {
        boolean[] removed = {false};
        this.subscriptions.computeIfPresent(topic, (_, list) -> {
            if (list.isEmpty()) {
                removed[0] = true;
                return null;
            }
            return list;
        });
        if (removed[0]) {
            this.unsubscribeAtBroker(topic);
        }
    }

    private void unsubscribeAtBroker(String topic) {
        MqttClient client = this.mqttClient;
        if (client == null || !client.isConnected()) {
            return;
        }
        try {
            client.unsubscribe(topic);
        } catch (MqttException e) {
            STEMApp.LOGGER.ERROR(e);
        }
    }

    private void resubscribeAll() {
        MqttClient client = this.mqttClient;
        if (client == null) {
            return;
        }
        for (String topic : List.copyOf(this.subscriptions.keySet())) {
            try {
                client.subscribe(topic, DEFAULT_QOS, new TopicDispatcher(topic));
            } catch (MqttException e) {
                STEMApp.LOGGER.ERROR("Could not restore MQTT subscription " + topic);
                STEMApp.LOGGER.ERROR(e);
            }
        }
        STEMApp.LOGGER.INFO("Restored " + this.subscriptions.size() + " MQTT subscriptions");
    }

    private record Subscription(IMqttMessageListener listener, STEMPlugin owner) {
    }

    /**
     * Receives messages for one topic filter on the Paho thread and hands them to the dispatch thread.
     */
    private final class TopicDispatcher implements IMqttMessageListener {
        private final String topicFilter;

        private TopicDispatcher(String topicFilter) {
            this.topicFilter = topicFilter;
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            try {
                dispatcher.execute(() -> {
                    List<Subscription> list = subscriptions.get(this.topicFilter);
                    if (list == null) {
                        return;
                    }
                    for (Subscription subscription : list) {
                        try {
                            subscription.listener.messageArrived(topic, message);
                        } catch (Throwable throwable) {
                            STEMApp.LOGGER.ERROR("Error in MQTT listener for " + topic + ": " + throwable);
                            if (throwable instanceof Exception exception) {
                                STEMApp.LOGGER.ERROR(exception);
                            }
                        }
                    }
                });
            } catch (RejectedExecutionException ignored) {
                /* module is shutting down */
            }
        }
    }

    private final class ConnectionCallback implements MqttCallbackExtended {

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            if (reconnect) {
                STEMApp.LOGGER.INFO("Reconnected to IOBroker " + serverURI);
            }
            /* Subscribing blocks until the broker acknowledges, so never do it on the Paho callback thread */
            Thread.ofVirtual().name("STEM-MQTT-Resubscribe").start(MqttModule.this::resubscribeAll);
        }

        @Override
        public void connectionLost(Throwable cause) {
            if (!shuttingDown) {
                STEMApp.LOGGER.WARNING("Connection to IOBroker lost: " + cause + ". Reconnecting automatically...");
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            /* messages without a matching listener are ignored */
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    }
}
