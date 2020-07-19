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

import java.util.List;

public class HelpCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        List<String> commands = STEMSystemApp.getInstance().getCommandModule().getCommandList();
        StringBuilder stringBuilder = new StringBuilder("Commands: ");
        for (int i = 0; i < commands.size(); i++) {
            String command = commands.get(i);
            stringBuilder.append(command);
            if (i < commands.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        STEMSystemApp.LOGGER.LIVE(stringBuilder.toString());
        return true;
    }

}
