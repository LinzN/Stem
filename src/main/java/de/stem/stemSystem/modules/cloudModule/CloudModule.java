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

package de.stem.stemSystem.modules.cloudModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import org.aarboard.nextcloud.api.NextcloudConnector;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.io.IOException;


public class CloudModule extends AbstractModule {

    STEMSystemApp stemSystemApp;
    private FileConfiguration fileConfiguration;
    private NextcloudConnector nextcloudConnector;

    private boolean isEnabled;
    private String cloudURL;
    private int cloudPort;
    private String cloudUser;
    private String cloudPassToken;

    public CloudModule(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
        this.initConfig();
        if(this.isEnabled) {
            this.initCloudSetup();
        }
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_cloud.yml"));
        this.isEnabled = this.fileConfiguration.getBoolean("enabled", true);
        this.cloudURL = this.fileConfiguration.getString("url", "cloud.de");
        this.cloudPort = this.fileConfiguration.getInt("port", 443);
        this.cloudUser = this.fileConfiguration.getString("user", "testuser");
        this.cloudPassToken = this.fileConfiguration.getString("passToken", "GeheimesPW");
        this.fileConfiguration.save();
    }

    private void initCloudSetup(){
        this.nextcloudConnector = new NextcloudConnector(this.cloudURL, true, cloudPort, cloudUser, cloudPassToken);
    }

    @Override
    public void onShutdown() {
        try {
            this.nextcloudConnector.shutdown();
        } catch (IOException e) {
            STEMSystemApp.LOGGER.ERROR(e);
        }
    }

}
