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

package de.stem.stemSystem.configuration;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.stem.stemSystem.STEMSystemApp;

import java.io.File;
import java.util.logging.Level;

public class AppConfiguration {

    /* Variables */
    FileConfiguration configFile;
    private String fileName = "STEM-Settings.yml";
    private STEMSystemApp stemSystemApp;
    public Level logLevel;

    /* Create class instance */
    public AppConfiguration(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
        this.init();
    }


    /* Load file*/
    public void init() {
        this.configFile = YamlConfiguration.loadConfiguration(new File(this.fileName));
        this.load();
        this.configFile.save();
    }

    /* Load the file in memory */
    public void load() {
        this.logLevel = Level.parse(this.configFile.getString("system.logLevel", Level.ALL.getName()));
    }

}
