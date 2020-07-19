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


import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;
import de.stem.stemSystem.utils.Color;

import java.util.ArrayList;

public class PluginsCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        ArrayList<STEMPlugin> plugins = STEMSystemApp.getInstance().getPluginModule().getLoadedPlugins();
        StringBuilder stringBuilder = new StringBuilder("Loaded plugins (" + plugins.size() + "): ");
        for (int i = 0; i < plugins.size(); i++) {
            STEMPlugin plugin = plugins.get(i);
            stringBuilder.append(Color.GREEN).append(plugin.getPluginName()).append(":").append(plugin.getVersion()).append(Color.RESET);
            if (i < plugins.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        STEMSystemApp.LOGGER.LIVE(stringBuilder.toString());
        return true;
    }

}
