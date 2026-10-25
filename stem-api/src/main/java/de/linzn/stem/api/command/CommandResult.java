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

import java.util.List;

/**
 * Result of {@link CommandService#execute(String, java.time.Duration)}.
 *
 * @param found    false if no command with this name is registered
 * @param finished false if the command was still running when the timeout was reached
 * @param success  true if the command finished without an exception and reported success
 * @param output   log lines the command wrote while it ran, oldest first
 */
public record CommandResult(boolean found, boolean finished, boolean success, List<String> output) {

    public CommandResult {
        output = List.copyOf(output);
    }

    public static CommandResult notFound(String commandName) {
        return new CommandResult(false, true, false, List.of("Unknown command: " + commandName));
    }
}
