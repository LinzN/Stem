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

package de.linzn.stem.modules.commandModule;

import de.linzn.simplyLogger.input.InputHandler;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.commandModule.defaultCommands.CommandSetup;
import de.linzn.stem.modules.pluginModule.PluginModule;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandModule extends AbstractModule implements InputHandler {
    private final CommandSetup commandSetup;
    /* command name -> owning plugin (only for commands registered by plugins) */
    private final ConcurrentHashMap<String, STEMPlugin> commandOwners;

    public CommandModule(STEMApp stemApp) {
        super(stemApp);
        this.commandOwners = new ConcurrentHashMap<>();
        this.commandSetup = new CommandSetup(this.getStemApp());
        STEMApp.logSystem.registerInputHandler(this);
    }

    @Override
    public void onConsoleInput(String s, String[] strings) {
        this.commandSetup.runCommand(s, strings);
    }

    /**
     * Register a command. The owner plugin is detected automatically.
     *
     * @param command  command name
     * @param ICommand command executor
     */
    public void registerCommand(String command, ICommand ICommand) {
        this.registerCommand(command, ICommand, PluginModule.findOwner(ICommand.getClass()));
    }

    /**
     * Register a command for a plugin.
     *
     * @param command  command name
     * @param ICommand command executor
     * @param owner    owning plugin, may be null for core commands
     */
    public void registerCommand(String command, ICommand ICommand, STEMPlugin owner) {
        if (command == null || command.isBlank() || ICommand == null) {
            STEMApp.LOGGER.ERROR("Invalid command registration: " + command);
            return;
        }
        String commandName = command.toLowerCase(Locale.ROOT);
        STEMApp.LOGGER.INFO("Register new command #" + commandName);
        ICommand previous = commandSetup.terminalExecutes.put(commandName, ICommand);
        if (previous != null && previous != ICommand) {
            STEMApp.LOGGER.WARNING("Command #" + commandName + " was already registered and has been replaced");
        }
        if (owner != null) {
            this.commandOwners.put(commandName, owner);
        } else {
            this.commandOwners.remove(commandName);
        }
    }

    public void unregisterCommand(String command) {
        if (command == null) {
            return;
        }
        String commandName = command.toLowerCase(Locale.ROOT);
        commandSetup.terminalExecutes.remove(commandName);
        this.commandOwners.remove(commandName);
    }

    /**
     * Unregister all commands of a plugin
     *
     * @param owner plugin to remove the commands for
     */
    public void unregisterCommands(STEMPlugin owner) {
        for (Map.Entry<String, STEMPlugin> entry : this.commandOwners.entrySet()) {
            if (entry.getValue() == owner) {
                this.unregisterCommand(entry.getKey());
                STEMApp.LOGGER.DEBUG("Command unregister: #" + entry.getKey() + " from " + owner.getPluginName());
            }
        }
    }

    public List<String> getCommandList() {
        return new ArrayList<>(this.commandSetup.terminalExecutes.keySet());
    }

    @Override
    public void onShutdown() {
        STEMApp.logSystem.unregisterInputHandler();
    }
}
