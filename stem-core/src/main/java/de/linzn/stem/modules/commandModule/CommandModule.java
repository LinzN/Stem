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
import de.linzn.stem.api.command.CommandResult;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.commandModule.defaultCommands.CommandSetup;
import de.linzn.stem.modules.pluginModule.PluginModule;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogRecord;

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

    /**
     * Execute a command line on its own thread and wait for it. The log lines the command writes on that
     * thread are captured as output.
     *
     * @param commandLine command and arguments separated by whitespace
     * @param timeout     maximum time to wait
     * @param caller      plugin that requested the execution, for the log; may be null
     */
    public CommandResult executeCommand(String commandLine, Duration timeout, STEMPlugin caller) {
        String line = commandLine == null ? "" : commandLine.trim();
        if (line.isEmpty()) {
            return CommandResult.notFound("");
        }
        String[] parts = line.split("\\s+");
        String commandName = parts[0].toLowerCase(Locale.ROOT);
        String[] arguments = Arrays.copyOfRange(parts, 1, parts.length);
        ICommand command = this.commandSetup.terminalExecutes.get(commandName);
        if (command == null) {
            return CommandResult.notFound(commandName);
        }
        STEMApp.LOGGER.INFO("Execute command '" + line + "'" + (caller != null ? " for plugin " + caller.getPluginName() : ""));
        long start = System.currentTimeMillis();
        boolean[] success = {false};
        Thread worker = Thread.ofVirtual().name("STEM-Command-" + commandName).unstarted(() -> {
            try {
                success[0] = command.executeTerminal(arguments);
            } catch (Exception e) {
                STEMApp.LOGGER.ERROR("Command " + commandName + " failed: " + e);
                STEMApp.LOGGER.ERROR(e);
            }
        });
        worker.start();
        boolean finished;
        try {
            finished = worker.join(timeout == null ? Duration.ofSeconds(60) : timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            finished = false;
        }
        long threadId = worker.threadId();
        List<String> output = new ArrayList<>();
        for (LogRecord logRecord : STEMApp.LOGGER.getLastEntries(5000)) {
            if (logRecord.getLongThreadID() == threadId && logRecord.getMillis() >= start) {
                output.add(logRecord.getMessage());
            }
        }
        return new CommandResult(true, finished, finished && success[0], output);
    }

    @Override
    public void onShutdown() {
        STEMApp.logSystem.unregisterInputHandler();
    }
}
