/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem.modules.mqttModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;


public class MqttModule extends AbstractModule {

    STEMSystemApp stemSystemApp;
    private FileConfiguration fileConfiguration;
    private String broker;
    private String clientId;
    private String user;
    private String password;

    private MqttClient mqttClient;


    public MqttModule(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
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
            STEMSystemApp.LOGGER.INFO("Connecting to IOBroker " + broker + "...");
            mqttClient.connect(connOpts);
            STEMSystemApp.LOGGER.INFO("Connection to IOBroker is valid!");

            MqttMessage mqttMessage = new MqttMessage("Hello".getBytes());
            mqttMessage.setQos(2);
            mqttClient.publish("stem-system/test", mqttMessage);

        } catch (MqttException e) {
            STEMSystemApp.LOGGER.ERROR(e);
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

            STEMSystemApp.LOGGER.INFO("Disconnecting from IOBroker...");
            this.mqttClient.disconnect();
            STEMSystemApp.LOGGER.INFO("Disconnected from IOBroker!");
            this.mqttClient.close();
        } catch (MqttException e) {
            STEMSystemApp.LOGGER.ERROR(e);
        }
    }

    public boolean publish(String topic, MqttMessage mqttMessage) {
        try {
            this.mqttClient.publish(topic, mqttMessage);
            return true;
        } catch (MqttException e) {
            STEMSystemApp.LOGGER.ERROR(e);
            return false;
        }
    }

    public boolean subscribe(String topic, IMqttMessageListener iMqttMessageListener) {
        try {
            this.mqttClient.subscribe(topic, iMqttMessageListener);
            return true;
        } catch (MqttException e) {
            STEMSystemApp.LOGGER.ERROR(e);
            return false;
        }
    }
}
