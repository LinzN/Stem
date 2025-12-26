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

package de.linzn.stem.modules;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import de.linzn.stem.utils.JavaUtils;

import java.io.File;

public abstract class AbstractModule {
    private STEMPlugin modulePlugin;

    public AbstractModule() {
        this.modulePlugin = this.setupModulePlugin(this.getClass().getSimpleName());
        STEMApp.LOGGER.CORE("Load module " + this.modulePlugin.getPluginName());
    }

    public STEMPlugin getModulePlugin() {
        return this.modulePlugin;
    }

    public void shutdownModule() {
        STEMApp.LOGGER.CORE("Unload module " + this.modulePlugin.getPluginName());
        this.onShutdown();
        STEMApp.getInstance().getCallBackService().unregisterCallbackListeners(this.modulePlugin);
        STEMApp.getInstance().getScheduler().cancelTasks(this.modulePlugin);
    }

    public abstract void onShutdown();

    private STEMPlugin setupModulePlugin(String moduleName) {
        return this.modulePlugin = new STEMPlugin() {

            @Override
            public void onEnable() {
            }

            @Override
            public void onDisable() {
            }

            @Override
            public String getPluginName() {
                return moduleName;
            }

            @Override
            public String getVersion() {
                return JavaUtils.getVersion();
            }

            @Override
            public String getClassPath() {
                return null;
            }

            @Override
            public String getDescription() {
                return getPluginName() + "::" + getVersion();
            }

            @Override
            public File getDataFolder() {
                return null;
            }

            @Override
            public FileConfiguration getDefaultConfig() {
                return null;
            }
        };
    }
}
