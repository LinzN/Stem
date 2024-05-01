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
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;
import de.stem.stemSystem.taskManagment.AbstractCallback;
import de.stem.stemSystem.taskManagment.TaskMeta;
import de.stem.stemSystem.utils.JavaUtils;

import java.util.HashMap;
import java.util.HashSet;

public class UpdateCheckCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        STEMSystemApp.LOGGER.INFO("Starting Update check for all available plugins!");
        STEMSystemApp.getInstance().getPluginModule().getUpdateCheck().checkForUpdates();
        STEMSystemApp.LOGGER.INFO("Check done");
        return true;
    }

}
