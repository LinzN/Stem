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

package de.stem.stemSystem.modules.pluginModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;

import java.io.File;

public abstract class AZPlugin {
    private String pluginName;
    private String version;
    private String classPath;
    private File dataFolder;
    private FileConfiguration defaultConfig;

    public void setUp(String pluginName, String version, String classPath) {
        this.pluginName = pluginName;
        this.version = version;
        this.classPath = classPath;
        this.dataFolder = new File(PluginModule.pluginDirectory, pluginName);
        this.defaultConfig = YamlConfiguration.loadConfiguration(new File(dataFolder, "config.yml"));
    }

    public abstract void onEnable();

    public abstract void onDisable();

    public String getPluginName() {
        return this.pluginName;
    }

    public String getVersion() {
        return this.version;
    }

    public String getClassPath() {
        return this.classPath;
    }

    public String getDescription() {
        return this.pluginName + "::" + this.version;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public FileConfiguration getDefaultConfig() {
        return defaultConfig;
    }
}
