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

package de.stem.stemSystem.modules.commandModule.defaultCommands;


import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;

public class ReloadCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        STEMSystemApp.LOGGER.LIVE("Reloading plugins ...");
        STEMSystemApp.getInstance().getPluginModule().reloadPlugins();
        STEMSystemApp.LOGGER.LIVE("Plugins reloaded!");
        return true;
    }

}
