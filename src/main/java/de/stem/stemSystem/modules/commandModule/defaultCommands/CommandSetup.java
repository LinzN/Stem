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

import de.stem.stemSystem.AppLogger;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;

import java.util.TreeMap;

public class CommandSetup {
    public TreeMap<String, ICommand> terminalExecutes;
    private STEMSystemApp stemSystemApp;

    public CommandSetup(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
        this.terminalExecutes = new TreeMap<>();
        this.initialTerminalExecudes();
    }

    public void runCommand(String command, String[] args) {
        this.stemSystemApp.getScheduler().runTask(this.stemSystemApp.getCommandModule().getModulePlugin(), () -> {
            try {
                if (this.terminalExecutes.containsKey(command.toLowerCase())) {
                    this.terminalExecutes.get(command.toLowerCase()).executeTerminal(args);
                } else {
                    AppLogger.logger("Error on command", false);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        });
    }

    private void initialTerminalExecudes() {
        registerCommand("stop", new StopCommand());
        registerCommand("loadplugin", new LoadPluginCommand());
        registerCommand("plugins", new PluginsCommand());
        registerCommand("reload", new ReloadCommand());
        registerCommand("push", new PushCommand());
        registerCommand("help", new HelpCommand());
        registerCommand("verbose", new VerboseCommand());
        registerCommand("status", new StatusCommand());
        registerCommand("uptime", new UptimeCommand());
    }

    private void registerCommand(String command, ICommand ICommand) {
        AppLogger.logger("Register internal command #" + command, true);
        terminalExecutes.put(command, ICommand);
    }
}
