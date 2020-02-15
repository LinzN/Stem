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

package de.azcore.azcoreRuntime.module.terminal;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.internal.containers.Module;
import de.azcore.azcoreRuntime.module.terminal.commands.CommandSetup;
import de.azcore.azcoreRuntime.module.terminal.commands.ICommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerminalModule extends Module implements Runnable {
    private AZCoreRuntimeApp azCoreRuntime;
    private CommandSetup commandSetup;

    public TerminalModule(AZCoreRuntimeApp azCoreRuntime) {
        this.azCoreRuntime = azCoreRuntime;
        this.azCoreRuntime.getHeartbeat().runTask(this);
        this.commandSetup = new CommandSetup(this.azCoreRuntime);
    }

    @Override
    public void run() {
        while (this.azCoreRuntime.getHeartbeat().isAlive()) {
            String input = System.console().readLine();

            String[] inputArray = input.split(" ");
            String command = inputArray[0];

            String[] args = Arrays.copyOfRange(inputArray, 1, inputArray.length);
            this.commandSetup.runCommand(command, args);
        }
    }

    public void registerCommand(String command, ICommand ICommand) {
        AZCoreRuntimeApp.logger("Register new command #" + command, true, false);
        commandSetup.terminalExecutes.put(command, ICommand);
    }

    public void unregisterCommand(String command) {
        commandSetup.terminalExecutes.remove(command);
    }

    public List<String> getCommandList() {
        return new ArrayList<>(this.commandSetup.terminalExecutes.keySet());
    }
}
