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

package de.azcore.azcoreRuntime.module.terminal.commands;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;

public class VerboseCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        boolean value = AZCoreRuntimeApp.getVerbose();
        value = !value;
        System.out.println("Set verbose to " + value);
        AZCoreRuntimeApp.setVerbose(value);
        return true;
    }

}
