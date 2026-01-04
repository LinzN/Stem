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

public class RebootCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        StemRestartOperation stemRestartOperation = new StemRestartOperation();
        STEMApp.LOGGER.CORE("Stem framework will reboot in 5 seconds!");
        STEMApp.getInstance().getScheduler().runTaskLater(STEMApp.getInstance().getScheduler().getDefaultSystemPlugin(), stemRestartOperation, 5, TimeUnit.SECONDS);
        return true;
    }

}
