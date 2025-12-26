/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
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

import java.util.List;

public class HelpCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        List<String> commands = STEMApp.getInstance().getCommandModule().getCommandList();
        StringBuilder stringBuilder = new StringBuilder("Commands: ");
        for (int i = 0; i < commands.size(); i++) {
            String command = commands.get(i);
            stringBuilder.append(command);
            if (i < commands.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        STEMApp.LOGGER.LIVE(stringBuilder.toString());
        return true;
    }

}
