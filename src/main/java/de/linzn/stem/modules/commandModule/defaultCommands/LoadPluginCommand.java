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

package de.linzn.stem.modules.commandModule.defaultCommands;


import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.commandModule.ICommand;
import de.linzn.stem.modules.pluginModule.PluginModule;

import java.io.File;
import java.io.IOException;

public class LoadPluginCommand implements ICommand {


    @Override
    public boolean executeTerminal(String[] args) {
        if (args.length > 0) {
            String arg = args[0].replace(".jar", "");
            File file = new File(PluginModule.pluginDirectory, arg + ".jar");
            if (file.exists()) {
                try {
                    STEMApp.getInstance().getPluginModule().loadPlugin(file, true);
                } catch (IOException e) {
                    STEMApp.LOGGER.ERROR(e);
                }
            } else {
                STEMApp.LOGGER.LIVE("No plugin file found!");
            }
        }
        return true;
    }

}
