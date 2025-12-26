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

package de.linzn.stem;

import de.linzn.simplyLogger.LogSystem;
import de.linzn.simplyLogger.Logger;
import de.linzn.stem.configuration.AppConfiguration;
import de.linzn.stem.modules.cloudModule.CloudModule;
import de.linzn.stem.modules.commandModule.CommandModule;
import de.linzn.stem.modules.complianceModule.ComplianceModule;
import de.linzn.stem.modules.databaseModule.DatabaseModule;
import de.linzn.stem.modules.eventModule.EventModule;
import de.linzn.stem.modules.eventModule.events.StemStartupEvent;
import de.linzn.stem.modules.healthModule.HealthModule;
import de.linzn.stem.modules.informationModule.InformationModule;
import de.linzn.stem.modules.libraryModule.LibraryModule;
import de.linzn.stem.modules.libraryModule.StemClassLoader;
import de.linzn.stem.modules.mqttModule.MqttModule;
import de.linzn.stem.modules.notificationModule.NotificationModule;
import de.linzn.stem.modules.pluginModule.PluginModule;
import de.linzn.stem.modules.scriptModule.ScriptManager;
import de.linzn.stem.modules.stemLinkModule.StemLinkModule;
import de.linzn.stem.taskManagment.CallbackService;
import de.linzn.stem.taskManagment.SchedulerService;
import de.linzn.stem.taskManagment.StemKernel;
import de.linzn.stem.utils.JavaUtils;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class STEMApp {

    public static Logger LOGGER;
    public static LogSystem logSystem;
    private static STEMApp instance;
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
    private ComplianceModule complianceModule;
    private DatabaseModule databaseModule;
    private PluginModule pluginModule;
    private CloudModule cloudModule;


    public STEMApp(String[] args) {
        instance = this;
        STEMApp.LOGGER.CORE("STEM version " + JavaUtils.getVersion());
        this.start_time = System.nanoTime();
        this.isActive = new AtomicBoolean(true);
        this.stemKernel = new StemKernel();
        Thread main = new Thread(this.stemKernel);
        main.setName(JavaUtils.getKernelName());
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
        STEMApp.LOGGER.CORE(STEMApp.class.getSimpleName() + " load mainframe...");
        new STEMApp(args);
    }

    public static STEMApp getInstance() {
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
        complianceModule = new ComplianceModule(instance);
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
        this.complianceModule.shutdownModule();
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
        STEMApp.LOGGER.CORE("Shutdown complete!");
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

    public ComplianceModule getComplianceModule(){
        return complianceModule;
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
