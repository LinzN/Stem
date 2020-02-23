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

package de.azcore.azcoreRuntime.modules.commandModule;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.modules.AbstractModule;
import de.azcore.azcoreRuntime.modules.commandModule.defaultCommands.CommandSetup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandModule extends AbstractModule implements Runnable {
    private AZCoreRuntimeApp azCoreRuntime;
    private CommandSetup commandSetup;
    private boolean moduleAlive;

    public CommandModule(AZCoreRuntimeApp azCoreRuntime) {
        this.azCoreRuntime = azCoreRuntime;
        this.moduleAlive = true;
        this.commandSetup = new CommandSetup(this.azCoreRuntime);
        this.azCoreRuntime.getScheduler().runTask(this.getModulePlugin(), this);
    }

    @Override
    public void run() {
        while (moduleAlive) {
            String input = System.console().readLine();

            String[] inputArray = input.split(" ");
            String command = inputArray[0];

            String[] args = Arrays.copyOfRange(inputArray, 1, inputArray.length);
            this.commandSetup.runCommand(command, args);
        }
    }

    public void registerCommand(String command, ICommand ICommand) {
        AppLogger.logger("Register new command #" + command, true, false);
        commandSetup.terminalExecutes.put(command, ICommand);
    }

    public void unregisterCommand(String command) {
        commandSetup.terminalExecutes.remove(command);
    }

    public List<String> getCommandList() {
        return new ArrayList<>(this.commandSetup.terminalExecutes.keySet());
    }

    @Override
    public void onShutdown() {
        this.moduleAlive = false;
    }
}
