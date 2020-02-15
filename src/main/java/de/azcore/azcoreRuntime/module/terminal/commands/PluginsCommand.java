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
import de.azcore.azcoreRuntime.internal.utils.Color;
import de.azcore.azcoreRuntime.module.plugin.Plugin;

import java.util.ArrayList;

public class PluginsCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        ArrayList<Plugin> plugins = AZCoreRuntimeApp.getInstance().getPluginModule().getLoadedPlugins();
        StringBuilder stringBuilder = new StringBuilder("Loaded plugins (" + plugins.size() + "): ");
        for (int i = 0; i < plugins.size(); i++) {
            Plugin plugin = plugins.get(i);
            stringBuilder.append(Color.GREEN).append(plugin.getPluginName()).append(":").append(plugin.getVersion()).append(Color.RESET);
            if (i < plugins.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        AZCoreRuntimeApp.logger(stringBuilder.toString(), false, false);
        return true;
    }

}
