/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem;

import de.linzn.simplyLogger.LogSystem;
import de.linzn.simplyLogger.Logger;
import de.stem.stemSystem.configuration.AppConfiguration;
import de.stem.stemSystem.modules.cloudModule.CloudModule;
import de.stem.stemSystem.modules.commandModule.CommandModule;
import de.stem.stemSystem.modules.databaseModule.DatabaseModule;
import de.stem.stemSystem.modules.eventModule.EventModule;
import de.stem.stemSystem.modules.eventModule.events.StemStartupEvent;
import de.stem.stemSystem.modules.healthModule.HealthModule;
import de.stem.stemSystem.modules.informationModule.InformationModule;
import de.stem.stemSystem.modules.libraryModule.LibraryModule;
import de.stem.stemSystem.modules.libraryModule.StemClassLoader;
import de.stem.stemSystem.modules.mqttModule.MqttModule;
import de.stem.stemSystem.modules.notificationModule.NotificationModule;
import de.stem.stemSystem.modules.pluginModule.PluginModule;
import de.stem.stemSystem.modules.scriptModule.ScriptManager;
import de.stem.stemSystem.modules.stemLinkModule.StemLinkModule;
import de.stem.stemSystem.taskManagment.CallbackService;
import de.stem.stemSystem.taskManagment.SchedulerService;
import de.stem.stemSystem.taskManagment.StemKernel;
import de.stem.stemSystem.utils.JavaUtils;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class STEMSystemApp {

    public static Logger LOGGER;
    public static LogSystem logSystem;
    private static STEMSystemApp instance;
    private final AtomicBoolean isActive;
    private final StemKernel stemKernel;
    private final Date uptimeDate;
    private final long start_time;
    private StemClassLoader stemClassLoader;
    private AppConfiguration appConfiguration;
    private EventModule eventModule;
    private StemLinkModule stemLinkModule;
    private LibraryModule libraryModule;
    private MqttModule mqttModule;
    private CommandModule commandModule;
    private NotificationModule notificationModule;
    private InformationModule informationModule;
    private ScriptManager scriptManager;
    private HealthModule healthModule;
    private DatabaseModule databaseModule;
    private PluginModule pluginModule;
    private CloudModule cloudModule;


    public STEMSystemApp(String[] args) {
        instance = this;
        STEMSystemApp.LOGGER.CORE("STEM version " + JavaUtils.getVersion());
        this.start_time = System.nanoTime();
        this.isActive = new AtomicBoolean(true);
        this.stemKernel = new StemKernel();
        Thread main = new Thread(this.stemKernel);
        main.setName("STEM");
        main.start();
        this.uptimeDate = new Date();
        this.stemKernel.getSchedulerService().runTaskInCore(this.stemKernel.getSchedulerService().getDefaultSystemPlugin(), () -> {
            loadModules();
            logSystem.setLogLevel(this.appConfiguration.logLevel);
            int startupTime = (int) ((System.nanoTime() - start_time) / 1e6);
            StemStartupEvent stemStartupEvent = new StemStartupEvent(startupTime);
            this.eventModule.getStemEventBus().fireEvent(stemStartupEvent);
        });
    }

    public static void main(String[] args) {
        logSystem = new LogSystem("STEM");
        logSystem.setFileLogger(new File("logs"));
        logSystem.setLogLevel(Level.ALL);
        LOGGER = logSystem.getLogger();
        STEMSystemApp.LOGGER.CORE(STEMSystemApp.class.getSimpleName() + " load mainframe...");
        new STEMSystemApp(args);
    }

    public static STEMSystemApp getInstance() {
        return instance;
    }


    private void loadModules() {
        appConfiguration = new AppConfiguration(instance);
        eventModule = new EventModule(instance);
        databaseModule = new DatabaseModule(instance);
        stemLinkModule = new StemLinkModule(instance);
        mqttModule = new MqttModule(instance);
        notificationModule = new NotificationModule(instance);
        informationModule = new InformationModule(instance);
        scriptManager = new ScriptManager(instance);
        healthModule = new HealthModule(instance);
        commandModule = new CommandModule(instance);
        libraryModule = new LibraryModule(instance);
        pluginModule = new PluginModule(instance);
        cloudModule = new CloudModule(instance);
    }


    public boolean isActive() {
        return this.isActive.get();
    }

    public void shutdown() {
        this.cloudModule.shutdownModule();
        this.commandModule.shutdownModule();
        this.scriptManager.shutdownModule();
        this.pluginModule.shutdownModule();
        this.stemLinkModule.shutdownModule();
        this.mqttModule.shutdownModule();
        this.notificationModule.shutdownModule();
        this.informationModule.shutdownModule();
        this.healthModule.shutdownModule();
        this.databaseModule.shutdownModule();
        this.libraryModule.shutdownModule();
        this.eventModule.shutdownModule();
        this.stemKernel.endCore();
        this.isActive.set(false);
        STEMSystemApp.LOGGER.CORE("Shutdown complete!");
        System.exit(0);
    }

    public void setClassLoader(StemClassLoader stemClassLoader) {
        this.stemClassLoader = stemClassLoader;
    }

    public AppConfiguration getConfiguration() {
        return appConfiguration;
    }

    public SchedulerService getScheduler() {
        return this.stemKernel.getSchedulerService();
    }

    public CallbackService getCallBackService() {
        return this.stemKernel.getCallbackService();
    }

    public StemLinkModule getStemLinkModule() {
        return stemLinkModule;
    }

    public EventModule getEventModule() {
        return eventModule;
    }

    public MqttModule getMqttModule() {
        return mqttModule;
    }

    public ScriptManager getScriptManager() {
        return scriptManager;
    }

    public CommandModule getCommandModule() {
        return commandModule;
    }

    public NotificationModule getNotificationModule() {
        return notificationModule;
    }

    public InformationModule getInformationModule() {
        return informationModule;
    }

    public HealthModule getHealthModule() {
        return healthModule;
    }

    public DatabaseModule getDatabaseModule() {
        return databaseModule;
    }

    public PluginModule getPluginModule() {
        return pluginModule;
    }

    public CloudModule getCloudModule() {
        return cloudModule;
    }

    public StemClassLoader getStemClassLoader() {
        return stemClassLoader;
    }

    public Date getUptimeDate() {
        return this.uptimeDate;
    }

}
