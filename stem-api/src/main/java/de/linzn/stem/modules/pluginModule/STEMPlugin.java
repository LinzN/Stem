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
import de.linzn.stem.api.Stem;
import de.linzn.stem.api.StemApi;
import de.linzn.stem.api.StemLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base class of every STEM plugin.
 * <p>
 * Use {@link #getStemApi()} to access STEM. Everything registered through it is owned by this plugin
 * and removed automatically when the plugin is disabled.
 */
public abstract class STEMPlugin {
    private String pluginName;
    private String version;
    private String buildJobName;
    private String buildNumber;
    private String classPath;
    private File dataFolder;
    private volatile File tempFolder;
    private Path filePath;
    private FileConfiguration defaultConfig;
    private volatile StemApi stemApi;

    /* Called by the STEM plugin loader */
    void setUp(String pluginName, String version, String buildJobName, String buildNumber, String classPath, Path filePath, File dataFolder) {
        this.pluginName = pluginName;
        this.version = version;
        this.buildJobName = buildJobName;
        this.buildNumber = buildNumber;
        this.classPath = classPath;
        this.dataFolder = dataFolder;
        this.filePath = filePath;
        this.defaultConfig = YamlConfiguration.loadConfiguration(new File(dataFolder, "config.yml"));
    }

    public abstract void onEnable();

    public abstract void onDisable();

    /**
     * @return the STEM API bound to this plugin
     */
    public StemApi getStemApi() {
        StemApi api = this.stemApi;
        if (api == null) {
            api = Stem.api(this);
            this.stemApi = api;
        }
        return api;
    }

    /**
     * @return logger that prefixes messages with this plugin's name
     */
    public StemLogger getPluginLogger() {
        return this.getStemApi().logger();
    }

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

    /**
     * @return a private temp folder for this plugin, deleted when STEM stops
     */
    public synchronized File getTempFolder() {
        if (this.tempFolder != null) {
            return this.tempFolder;
        }

        try {
            File root = Stem.api().system().getTempFolder();
            this.tempFolder = Files.createTempDirectory(root.toPath(), this.getPluginName() + "_").toFile();
        } catch (IOException | RuntimeException e) {
            if (Stem.isAvailable()) {
                Stem.api().logger().error("Could not create temp folder for " + this.getPluginName(), e);
            }
        }
        return this.tempFolder;
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

    public Path getFilePath() {
        return filePath;
    }
}
