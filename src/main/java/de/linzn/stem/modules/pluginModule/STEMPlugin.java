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

package de.linzn.stem.modules.pluginModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;

import java.io.File;

public abstract class STEMPlugin {
    private String pluginName;
    private String version;
    private String buildJobName;
    private String buildNumber;
    private String classPath;
    private File dataFolder;
    private FileConfiguration defaultConfig;

    void setUp(String pluginName, String version, String buildJobName, String buildNumber, String classPath) {
        this.pluginName = pluginName;
        this.version = version;
        this.buildJobName = buildJobName;
        this.buildNumber = buildNumber;
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
        return this.pluginName + "::" + this.version + "::" + this.buildJobName + "::" + this.buildNumber;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public FileConfiguration getDefaultConfig() {
        return defaultConfig;
    }

    public String getBuildJobName() {
        return buildJobName;
    }

    public String getBuildNumber() {
        return buildNumber;
    }
}
