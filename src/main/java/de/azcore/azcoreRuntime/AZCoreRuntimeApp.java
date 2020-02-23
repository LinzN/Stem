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

package de.azcore.azcoreRuntime;

import de.azcore.azcoreRuntime.configuration.AppConfiguration;
import de.azcore.azcoreRuntime.modules.commandModule.CommandModule;
import de.azcore.azcoreRuntime.modules.databaseModule.DatabaseModule;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationModule;
import de.azcore.azcoreRuntime.modules.pluginModule.PluginModule;
import de.azcore.azcoreRuntime.modules.zSocketModule.ZSocketModule;
import de.azcore.azcoreRuntime.taskManagment.CallbackService;
import de.azcore.azcoreRuntime.taskManagment.CoreRunner;
import de.azcore.azcoreRuntime.taskManagment.SchedulerService;
import de.azcore.azcoreRuntime.utils.JavaUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class AZCoreRuntimeApp {

    private static AZCoreRuntimeApp instance;
    private AtomicBoolean isActive;

    private CoreRunner coreRunner;
    private AppConfiguration appConfiguration;
    private ZSocketModule zSocketModule;
    private CommandModule commandModule;
    private NotificationModule notificationModule;
    private DatabaseModule databaseModule;
    private PluginModule pluginModule;
    private long start_time;



    public AZCoreRuntimeApp(String[] args) {
        instance = this;
        AppLogger.logger("AZCore-Runtime version " + JavaUtils.getVersion(), true);
        this.start_time = System.nanoTime();
        this.isActive = new AtomicBoolean(true);
        this.coreRunner = new CoreRunner();
        Thread main = new Thread(this.coreRunner);
        main.setName("AZCore");
        main.start();
        this.coreRunner.getSchedulerService().runTaskInCore(this.coreRunner.getSchedulerService().getDefaultAZPlugin(), () -> {
            loadModules();
            AppLogger.logger("AZCore-Runtime startup finished in " + (int) ((System.nanoTime() - start_time) / 1e6) + " ms.", true);
        });
    }

    public static void main(String[] args) {
        AppLogger.logger(AZCoreRuntimeApp.class.getSimpleName() + " load mainframe...", false);
        new AZCoreRuntimeApp(args);
    }

    public static AZCoreRuntimeApp getInstance() {
        return instance;
    }


    private void loadModules() {
        appConfiguration = new AppConfiguration(instance);
        databaseModule = new DatabaseModule(instance);
        zSocketModule = new ZSocketModule(instance);
        notificationModule = new NotificationModule(instance);
        commandModule = new CommandModule(instance);
        pluginModule = new PluginModule(instance);
    }


    public boolean isActive() {
        return this.isActive.get();
    }

    public void shutdown() {
        this.commandModule.shutdownModule();
        this.pluginModule.shutdownModule();
        this.zSocketModule.shutdownModule();
        this.notificationModule.shutdownModule();
        this.databaseModule.shutdownModule();

        this.coreRunner.endCore();
        this.isActive.set(false);
        AppLogger.logger("Shutdown complete!", true);
        System.exit(0);
    }

    public AppConfiguration getConfiguration() {
        return appConfiguration;
    }

    public SchedulerService getScheduler() {
        return this.coreRunner.getSchedulerService();
    }

    public CallbackService getCallBackService() {
        return this.coreRunner.getCallbackService();
    }


    public ZSocketModule getZSocketModule() {
        return zSocketModule;
    }

    public CommandModule getCommandModule() {
        return commandModule;
    }

    public NotificationModule getNotificationModule() {
        return notificationModule;
    }

    public DatabaseModule getDatabaseModule() {
        return databaseModule;
    }

    public PluginModule getPluginModule() {
        return pluginModule;
    }

}
