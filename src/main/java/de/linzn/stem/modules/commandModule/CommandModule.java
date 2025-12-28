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

import java.util.ArrayList;
import java.util.List;

public class CommandModule extends AbstractModule implements InputHandler {
    private final CommandSetup commandSetup;

    public CommandModule(STEMApp stemApp) {
        super(stemApp);
        STEMApp.logSystem.registerInputHandler(this);
        this.commandSetup = new CommandSetup(this.getStemApp());
    }

    @Override
    public void onConsoleInput(String s, String[] strings) {
        this.commandSetup.runCommand(s, strings);
    }

    public void registerCommand(String command, ICommand ICommand) {
        STEMApp.LOGGER.INFO("Register new command #" + command);
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
        STEMApp.logSystem.unregisterInputHandler();

    }


}
