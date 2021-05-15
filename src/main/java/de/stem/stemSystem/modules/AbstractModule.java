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

package de.stem.stemSystem.modules;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;
import de.stem.stemSystem.utils.JavaUtils;

import java.io.File;

public abstract class AbstractModule {
    private STEMPlugin modulePlugin;

    public AbstractModule() {
        this.modulePlugin = this.setupModulePlugin(this.getClass().getSimpleName());
        STEMSystemApp.LOGGER.CORE("Load module " + this.modulePlugin.getPluginName());
    }

    public STEMPlugin getModulePlugin() {
        return this.modulePlugin;
    }

    public void shutdownModule() {
        STEMSystemApp.LOGGER.CORE("Unload module " + this.modulePlugin.getPluginName());
        this.onShutdown();
        STEMSystemApp.getInstance().getCallBackService().unregisterCallbackListeners(this.modulePlugin);
        STEMSystemApp.getInstance().getScheduler().cancelTasks(this.modulePlugin);
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
