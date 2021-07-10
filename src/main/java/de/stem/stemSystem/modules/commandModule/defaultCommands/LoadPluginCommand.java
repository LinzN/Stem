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

package de.stem.stemSystem.modules.commandModule.defaultCommands;


import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;
import de.stem.stemSystem.modules.pluginModule.PluginModule;

import java.io.File;
import java.io.IOException;

public class LoadPluginCommand implements ICommand {


    @Override
    public boolean executeTerminal(String[] args) {
        if (args.length > 0) {
            String arg = args[0].replace(".jar", "");
            File file = new File(PluginModule.pluginDirectory, arg + ".jar");
            if (file.exists()) {
                try {
                    STEMSystemApp.getInstance().getPluginModule().loadPlugin(file, true);
                } catch (IOException e) {
                    STEMSystemApp.LOGGER.ERROR(e);
                }
            } else {
                STEMSystemApp.LOGGER.LIVE("No plugin file found!");
            }
        }
        return true;
    }

}
