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

package de.linzn.stem.configuration;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.STEMApp;

import java.io.File;
import java.util.logging.Level;

public class AppConfiguration {

    public Level logLevel;

    public String healthCheckCronjob;
    /* Variables */
    FileConfiguration configFile;
    private String fileName = "STEM-Settings.yml";
    private STEMApp stemApp;

    /* Create class instance */
    public AppConfiguration(STEMApp stemApp) {
        this.stemApp = stemApp;
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
        this.healthCheckCronjob = this.configFile.getString("healthModule.checkCronjob", "0 1,7,13,19 * * *");
    }

}
