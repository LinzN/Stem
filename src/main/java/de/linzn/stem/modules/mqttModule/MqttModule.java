/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
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
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;


public class MqttModule extends AbstractModule {

    STEMApp stemApp;
    private FileConfiguration fileConfiguration;
    private String broker;
    private String clientId;
    private String user;
    private String password;

    private MqttClient mqttClient;


    public MqttModule(STEMApp stemApp) {
        this.stemApp = stemApp;
        this.initConfig();
        this.createClient();
    }

    private void createClient() {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqttClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(user);
            connOpts.setPassword(password.toCharArray());
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            STEMApp.LOGGER.INFO("Connecting to IOBroker " + broker + "...");
            mqttClient.connect(connOpts);
            STEMApp.LOGGER.INFO("Connection to IOBroker is valid!");

            MqttMessage mqttMessage = new MqttMessage("Hello".getBytes());
            mqttMessage.setQos(2);
            mqttClient.publish("stem-system/test", mqttMessage);

        } catch (MqttException e) {
            STEMApp.LOGGER.ERROR(e);
        }
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_mqtt.yml"));
        this.broker = this.fileConfiguration.getString("broker", "tcp://10.50.0.1:1883");
        this.clientId = this.fileConfiguration.getString("clientId", "STEM-SYSTEM");
        this.user = this.fileConfiguration.getString("user", "testuser");
        this.password = this.fileConfiguration.getString("password", "GeheimesPW");
        this.fileConfiguration.save();
    }

    @Override
    public void onShutdown() {
        try {
            MqttMessage mqttMessage = new MqttMessage("Bye".getBytes());
            mqttMessage.setQos(2);
            mqttClient.publish("stem-system/test", mqttMessage);

            STEMApp.LOGGER.INFO("Disconnecting from IOBroker...");
            this.mqttClient.disconnect();
            STEMApp.LOGGER.INFO("Disconnected from IOBroker!");
            this.mqttClient.close();
        } catch (MqttException e) {
            STEMApp.LOGGER.ERROR(e);
        }
    }

    public boolean publish(String topic, MqttMessage mqttMessage) {
        try {
            this.mqttClient.publish(topic, mqttMessage);
            return true;
        } catch (MqttException e) {
            STEMApp.LOGGER.ERROR(e);
            return false;
        }
    }

    public boolean subscribe(String topic, IMqttMessageListener iMqttMessageListener) {
        try {
            this.mqttClient.subscribe(topic, iMqttMessageListener);
            return true;
        } catch (MqttException e) {
            STEMApp.LOGGER.ERROR(e);
            return false;
        }
    }
}
