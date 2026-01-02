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

import java.util.TreeMap;

public class CommandSetup {
    private final STEMApp stemApp;
    public TreeMap<String, ICommand> terminalExecutes;

    public CommandSetup(STEMApp stemApp) {
        this.stemApp = stemApp;
        this.terminalExecutes = new TreeMap<>();
        this.initialTerminalExecudes();
    }

    public void runCommand(String command, String[] args) {
        this.stemApp.getScheduler().runTask(this.stemApp.getCommandModule().getModulePlugin(), () -> {
            try {
                if (this.terminalExecutes.containsKey(command.toLowerCase())) {
                    this.terminalExecutes.get(command.toLowerCase()).executeTerminal(args);
                } else {
                    STEMApp.LOGGER.LIVE("Error on command");
                }
            } catch (Exception e) {
                STEMApp.LOGGER.ERROR(e);
            }
        });
    }

    private void initialTerminalExecudes() {
        registerCommand("stop", new StopCommand());
        registerCommand("loadplugin", new LoadPluginCommand());
        registerCommand("plugins", new PluginsCommand());
        registerCommand("push", new PushCommand());
        registerCommand("help", new HelpCommand());
        registerCommand("verbose", new VerboseCommand());
        registerCommand("status", new StatusCommand());
        registerCommand("uptime", new UptimeCommand());
        registerCommand("healthcheck", new HealthCheckCommand());
        registerCommand("updatecheck", new UpdateCheckCommand());
        registerCommand("upgrade", new UpgradeCommand());
        registerCommand("runscript", new RunScriptCommand());
    }

    private void registerCommand(String command, ICommand ICommand) {
        STEMApp.LOGGER.INFO("Register internal command #" + command);
        terminalExecutes.put(command, ICommand);
    }
}
