/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.stem.stemSystem.modules.commandModule.defaultCommands;


import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;

import java.util.ArrayList;

public class PluginsCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        ArrayList<STEMPlugin> plugins = STEMSystemApp.getInstance().getPluginModule().getLoadedPlugins();
        StringBuilder stringBuilder = new StringBuilder("Loaded plugins (" + plugins.size() + "): ");
        for (int i = 0; i < plugins.size(); i++) {
            STEMPlugin plugin = plugins.get(i);
            stringBuilder.append(plugin.getPluginName()).append(":").append(plugin.getVersion());
            if (i < plugins.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        STEMSystemApp.LOGGER.LIVE(stringBuilder.toString());
        return true;
    }

}
