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

package de.azcore.azcoreRuntime.modules;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.modules.pluginModule.AZPlugin;
import de.linzn.simplyConfiguration.FileConfiguration;

import java.io.File;

public abstract class AbstractModule {
    private AZPlugin modulePlugin;

    public AbstractModule() {
        this.modulePlugin = this.setupModulePlugin(this.getClass().getSimpleName());
        AppLogger.logger("Load module " + this.modulePlugin.getPluginName(), true, false);
    }

    public AZPlugin getModulePlugin() {
        return this.modulePlugin;
    }

    public void shutdownModule() {
        AppLogger.logger("Unload module " + this.modulePlugin.getPluginName(), true, false);
        this.onShutdown();
        AZCoreRuntimeApp.getInstance().getCallBackService().unregisterCallbackListeners(this.modulePlugin);
        AZCoreRuntimeApp.getInstance().getScheduler().cancelTasks(this.modulePlugin);
    }

    public abstract void onShutdown();

    private AZPlugin setupModulePlugin(String moduleName) {
        return this.modulePlugin = new AZPlugin() {

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
                return AZCoreRuntimeApp.getInstance().getVersion();
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

            @Override
            public void setUp(String pluginName, String version, String classPath) {

            }
        };
    }
}
