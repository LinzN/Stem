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

package de.linzn.stem.core.api;

import de.linzn.stem.STEMApp;
import de.linzn.stem.api.command.CommandResult;
import de.linzn.stem.api.command.CommandService;
import de.linzn.stem.modules.commandModule.ICommand;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.time.Duration;
import java.util.List;

final class CommandServiceAdapter implements CommandService {
    private final STEMApp stemApp;
    private final STEMPlugin owner;

    CommandServiceAdapter(STEMApp stemApp, STEMPlugin owner) {
        this.stemApp = stemApp;
        this.owner = owner;
    }

    @Override
    public void register(String name, ICommand command) {
        this.stemApp.getCommandModule().registerCommand(name, command, owner);
    }

    @Override
    public void unregister(String name) {
        this.stemApp.getCommandModule().unregisterCommand(name);
    }

    @Override
    public List<String> getCommands() {
        return List.copyOf(this.stemApp.getCommandModule().getCommandList());
    }

    @Override
    public CommandResult execute(String commandLine, Duration timeout) {
        return this.stemApp.getCommandModule().executeCommand(commandLine, timeout, owner);
    }
}
