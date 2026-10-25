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

import java.util.Locale;
import java.util.concurrent.ConcurrentSkipListMap;

public class CommandSetup {
    private final STEMApp stemApp;
    /* Sorted and thread-safe: commands are registered by plugins while the console thread reads */
    public final ConcurrentSkipListMap<String, ICommand> terminalExecutes;

    public CommandSetup(STEMApp stemApp) {
        this.stemApp = stemApp;
        this.terminalExecutes = new ConcurrentSkipListMap<>();
        this.initialTerminalExecutes();
    }

    public void runCommand(String command, String[] args) {
        if (command == null || command.isBlank()) {
            return;
        }
        String commandName = command.toLowerCase(Locale.ROOT);
        String[] arguments = args == null ? new String[0] : args;
        this.stemApp.getScheduler().runTask(this.stemApp.getCommandModule().getModulePlugin(), () -> {
            ICommand iCommand = this.terminalExecutes.get(commandName);
            if (iCommand == null) {
                STEMApp.LOGGER.LIVE("Unknown command: " + commandName + " (type 'help' for a list of commands)");
                return;
            }
            try {
                iCommand.executeTerminal(arguments);
            } catch (Exception e) {
                STEMApp.LOGGER.ERROR(e);
            }
        });
    }

    private void initialTerminalExecutes() {
        registerCommand("stop", new StopCommand());
        registerCommand("reboot", new RebootCommand());
        registerCommand("plugins", new PluginsCommand());
        registerCommand("push", new PushCommand());
        registerCommand("help", new HelpCommand());
        registerCommand("verbose", new VerboseCommand());
        registerCommand("status", new StatusCommand());
        registerCommand("uptime", new UptimeCommand());
        registerCommand("healthcheck", new HealthCheckCommand());
        registerCommand("version", new VersionCommand());
        registerCommand("build", new BuildCommand());
        registerCommand("runscript", new RunScriptCommand());
    }

    private void registerCommand(String command, ICommand ICommand) {
        STEMApp.LOGGER.INFO("Register internal command #" + command);
        terminalExecutes.put(command, ICommand);
    }
}
