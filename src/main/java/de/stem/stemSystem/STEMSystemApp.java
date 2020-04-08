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

package de.stem.stemSystem;

import de.stem.stemSystem.configuration.AppConfiguration;
import de.stem.stemSystem.modules.commandModule.CommandModule;
import de.stem.stemSystem.modules.databaseModule.DatabaseModule;
import de.stem.stemSystem.modules.notificationModule.NotificationModule;
import de.stem.stemSystem.modules.pluginModule.PluginModule;
import de.stem.stemSystem.modules.zSocketModule.ZSocketModule;
import de.stem.stemSystem.taskManagment.CallbackService;
import de.stem.stemSystem.taskManagment.CoreRunner;
import de.stem.stemSystem.taskManagment.SchedulerService;
import de.stem.stemSystem.utils.JavaUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class STEMSystemApp {

    private static STEMSystemApp instance;
    private AtomicBoolean isActive;

    private CoreRunner coreRunner;
    private AppConfiguration appConfiguration;
    private ZSocketModule zSocketModule;
    private CommandModule commandModule;
    private NotificationModule notificationModule;
    private DatabaseModule databaseModule;
    private PluginModule pluginModule;
    private long start_time;


    public STEMSystemApp(String[] args) {
        instance = this;
        AppLogger.logger("STEM version " + JavaUtils.getVersion(), true);
        this.start_time = System.nanoTime();
        this.isActive = new AtomicBoolean(true);
        this.coreRunner = new CoreRunner();
        Thread main = new Thread(this.coreRunner);
        main.setName("STEM");
        main.start();
        this.coreRunner.getSchedulerService().runTaskInCore(this.coreRunner.getSchedulerService().getDefaultAZPlugin(), () -> {
            loadModules();
            AppLogger.logger("STEM-System startup finished in " + (int) ((System.nanoTime() - start_time) / 1e6) + " ms.", true);
        });
    }

    public static void main(String[] args) {
        AppLogger.logger(STEMSystemApp.class.getSimpleName() + " load mainframe...", false);
        new STEMSystemApp(args);
    }

    public static STEMSystemApp getInstance() {
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
