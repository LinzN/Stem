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

import de.linzn.simplyLogger.LOGLEVEL;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;

public class VerboseCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        boolean value = STEMSystemApp.logSystem.getLogLevel() == LOGLEVEL.DEBUG;
        value = !value;
        STEMSystemApp.LOGGER.LIVE("Set verbose to " + value);
        STEMSystemApp.logSystem.setLogLevel(value ? LOGLEVEL.DEBUG : LOGLEVEL.INFO);
        return true;
    }

}
