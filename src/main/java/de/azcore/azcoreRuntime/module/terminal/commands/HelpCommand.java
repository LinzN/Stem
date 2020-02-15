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

package de.azcore.azcoreRuntime.module.terminal.commands;


import de.azcore.azcoreRuntime.AZCoreRuntimeApp;

import java.util.List;

public class HelpCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        List<String> commands = AZCoreRuntimeApp.getInstance().getTerminalModule().getCommandList();
        StringBuilder stringBuilder = new StringBuilder("Commands: ");
        for (int i = 0; i < commands.size(); i++) {
            String command = commands.get(i);
            stringBuilder.append(command);
            if (i < commands.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        AZCoreRuntimeApp.logger(stringBuilder.toString(), false, false);
        return true;
    }

}
