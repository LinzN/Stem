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

package de.azcore.azcoreRuntime.modules.commandModule.defaultCommands;

import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.modules.commandModule.ICommand;

public class VerboseCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        boolean value = AppLogger.getVerbose();
        value = !value;
        System.out.println("Set verbose to " + value);
        AppLogger.setVerbose(value);
        return true;
    }

}
