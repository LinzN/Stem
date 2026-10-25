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

package de.linzn.stem.api.command;

import de.linzn.stem.modules.commandModule.ICommand;

import java.util.List;

/**
 * Console command registration. Command names are case-insensitive.
 */
public interface CommandService {

    /**
     * Register a command for the owner plugin. An existing command with the same name is replaced.
     */
    void register(String name, ICommand command);

    /**
     * Unregister a command.
     */
    void unregister(String name);

    /**
     * @return names of all registered commands, sorted
     */
    List<String> getCommands();
}
