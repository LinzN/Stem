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

package de.linzn.stem.modules.commandModule.defaultCommands;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.commandModule.ICommand;
import de.linzn.stem.taskManagment.operations.defaultOperations.StemRestartOperation;

import java.util.concurrent.TimeUnit;

public class BuildCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        if (args.length > 0) {
            String command = args[0];
            if (command.equalsIgnoreCase("update")) {
                STEMApp.LOGGER.INFO("Starting to check for available builds on MirraNET CI Server....");
                if (STEMApp.getInstance().getPluginModule().getUpdateCheck().checkForUpdates()) {
                    STEMApp.LOGGER.INFO("There are new builds available. To upgrade do 'build upgrade'. This will trigger a restart of the STEM Framework after upgrade!");
                }
                STEMApp.LOGGER.INFO("Check done");
            } else if (command.equalsIgnoreCase("upgrade")) {
                STEMApp.LOGGER.INFO("Starting to applying new builds for plugins and framework....");
                STEMApp.LOGGER.INFO("If there are new available builds to apply the STEM Framework will restart after upgrade!");
                int upgraded = STEMApp.getInstance().getPluginModule().getUpdateCheck().upgradeAvailableBuilds();
                if (upgraded > 0) {
                    STEMApp.LOGGER.INFO("Builds upgraded: " + upgraded);
                    STEMApp.LOGGER.INFO("Restart pending!");
                    STEMApp.LOGGER.CORE("Stem framework will reboot in 10 seconds!");
                    STEMApp.getInstance().getScheduler().runTaskLater(STEMApp.getInstance().getScheduler().getDefaultSystemPlugin(), new StemRestartOperation(), 10, TimeUnit.SECONDS);
                } else {
                    STEMApp.LOGGER.WARNING("There was no available builds to upgrade! Please run 'build update' first before upgrading!");
                }
            }
        }

        return true;
    }

}
