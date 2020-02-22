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
import de.azcore.azcoreRuntime.modules.pluginModule.AZPlugin;
import de.azcore.azcoreRuntime.modules.pluginModule.PluginModule;
import de.azcore.azcoreRuntime.modules.zSocketModule.ZSocketModule;
import de.azcore.azcoreRuntime.taskManagment.CoreRunner;
import de.azcore.azcoreRuntime.taskManagment.SchedulerService;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
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

    private AppLogger appLogger;


    public AZCoreRuntimeApp(String[] args) {
        instance = this;
        this.start_time = System.nanoTime();
        this.appLogger = new AppLogger();
        this.isActive = new AtomicBoolean(true);
        this.coreRunner = new CoreRunner();
        Thread main = new Thread(this.coreRunner);
        main.setName("AZCore");
        main.start();
        loadModules();
        finishStartup();
    }

    public static void main(String[] args) {
        AppLogger.logger(AZCoreRuntimeApp.class.getSimpleName() + " load mainframe...", false, false);
        new AZCoreRuntimeApp(args);
    }

    public static AZCoreRuntimeApp getInstance() {
        return instance;
    }


    private void loadModules() {
        AZPlugin azPlugin = this.coreRunner.getSchedulerService().getDefaultAZPlugin();
        this.coreRunner.getSchedulerService().runTaskInCore(azPlugin, () -> appConfiguration = new AppConfiguration(instance));
        this.coreRunner.getSchedulerService().runTaskInCore(azPlugin, () -> databaseModule = new DatabaseModule(instance));
        this.coreRunner.getSchedulerService().runTaskInCore(azPlugin, () -> zSocketModule = new ZSocketModule(instance));
        this.coreRunner.getSchedulerService().runTaskInCore(azPlugin, () -> notificationModule = new NotificationModule(instance));
        this.coreRunner.getSchedulerService().runTaskInCore(azPlugin, () -> commandModule = new CommandModule(instance));
        this.coreRunner.getSchedulerService().runTaskInCore(azPlugin, () -> pluginModule = new PluginModule(instance));
    }

    private void finishStartup() {
        Runnable finish = () -> AppLogger.logger("AZCore-Runtime startup finished in " + (int) ((System.nanoTime() - start_time) / 1e6) + " ms.", true, false);
        this.coreRunner.getSchedulerService().runTaskInCore(this.coreRunner.getSchedulerService().getDefaultAZPlugin(), finish);
    }

    public String getVersion() {
        String version;
        String res = "META-INF/maven/de.azcore/azcore-runtime/pom.properties";
        URL url = Thread.currentThread().getContextClassLoader().getResource(res);
        if (url == null) {
            version = "SS";
        } else {
            Properties props = new Properties();
            try {
                props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(res));
            } catch (IOException e) {
                e.printStackTrace();
            }
            version = props.getProperty("version");
        }

        return version;
    }

    public boolean isActive() {
        return this.isActive.get();
    }

    public void shutdown() {
        this.coreRunner.endCore();
        this.isActive.set(false);
        System.exit(0);
    }

    public AppConfiguration getConfiguration() {
        return appConfiguration;
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

    public SchedulerService getScheduler() {
        return this.coreRunner.getSchedulerService();
    }

}
