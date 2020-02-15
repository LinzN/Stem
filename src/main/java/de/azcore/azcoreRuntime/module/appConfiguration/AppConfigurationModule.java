/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.azcore.azcoreRuntime.module.appConfiguration;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.internal.containers.Module;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public class AppConfigurationModule extends Module {

    /* Variables */
    public String socketHost;
    public int socketPort;
    public String sqlHostName;
    public int sqlPort;
    public String sqlDatabaseName;
    public String sqlUserName;
    public String sqlPassword;
    public String cryptAESKey;
    public byte[] vector16B;
    FileConfiguration configFile;
    private AZCoreRuntimeApp azCoreRuntime;
    private String fileName = "azCoreSettings.yml";

    /* Create class instance */
    public AppConfigurationModule(AZCoreRuntimeApp azCoreRuntime) {
        this.azCoreRuntime = azCoreRuntime;
        this.init();

    }

    private static byte[] toByteArray(String string) {
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        byte[] result = new byte[strings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Byte.parseByte(strings[i]);
        }
        return result;
    }

    /* Load file*/
    public void init() {
        this.configFile = YamlConfiguration.loadConfiguration(new File(this.fileName));
        this.load();
        this.configFile.save();
    }

    /* Load the file in memory */
    public void load() {
        this.socketHost = this.configFile.getString("socketHost", "0.0.0.0");
        this.socketPort = this.configFile.getInt("socketPort", 11102);

        this.sqlHostName = this.configFile.getString("sqlHostname", "127.0.0.1");
        this.sqlPort = this.configFile.getInt("sqlPort", 3306);
        this.sqlDatabaseName = this.configFile.getString("sqlDatabaseName", "azcore_db");
        this.sqlUserName = this.configFile.getString("sqlUserName", "azcore");
        this.sqlPassword = this.configFile.getString("sqlPassword", "test123");

        this.cryptAESKey = this.configFile.getString("cryptAESKey", "3979244226452948404D635166546A576D5A7134743777217A25432A462D4A61");
        this.vector16B = toByteArray(this.configFile.getString("vector16B", Arrays.toString(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7})));
    }

}
