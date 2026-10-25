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

package de.linzn.stem;

import de.linzn.simplyLogger.LogSystem;
import de.linzn.simplyLogger.Logger;
import de.linzn.stem.configuration.AppConfiguration;
import de.linzn.stem.modules.AbstractModule;
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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Stream;

public class STEMApp {

    public static Logger LOGGER;
    public static LogSystem logSystem;
    private static volatile STEMApp instance;
    private final AtomicBoolean isActive;
    private final AtomicBoolean shutdownStarted;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final StemKernel stemKernel;
    private final Date uptimeDate;
    private final long start_time;
    private volatile File tempFolder;
    private volatile StemClassLoader stemClassLoader;
    private volatile AppConfiguration appConfiguration;
    private volatile EventModule eventModule;
    private volatile StemLinkModule stemLinkModule;
    private volatile LibraryModule libraryModule;
    private volatile MqttModule mqttModule;
    private volatile CommandModule commandModule;
    private volatile NotificationModule notificationModule;
    private volatile InformationModule informationModule;
    private volatile ScriptManager scriptManager;
    private volatile HealthModule healthModule;
    private volatile ComplianceModule complianceModule;
    private volatile DatabaseModule databaseModule;
    private volatile PluginModule pluginModule;
    private volatile CloudModule cloudModule;


    public STEMApp(String[] args) {
        instance = this;
        STEMApp.LOGGER.CORE("STEM version " + JavaUtils.getVersion());
        this.start_time = System.nanoTime();
        this.isActive = new AtomicBoolean(true);
        this.shutdownStarted = new AtomicBoolean(false);
        this.stemKernel = new StemKernel();
        Thread main = new Thread(this.stemKernel);
        main.setName(JavaUtils.getKernelName());
        main.start();
        this.uptimeDate = new Date();
        this.stemKernel.getSchedulerService().runTaskInCore(this.stemKernel.getSchedulerService().getDefaultSystemPlugin(), () -> {
            createTempFolder();
            loadModules();
            registerShutdownHook();
            logSystem.setLogLevel(this.appConfiguration.logLevel);
            int startupTime = (int) ((System.nanoTime() - start_time) / 1e6);
            StemStartupEvent stemStartupEvent = new StemStartupEvent(startupTime);
            this.eventModule.getStemEventBus().fireEvent(stemStartupEvent);
        });
    }

    static void main(String[] args) {
        logSystem = new LogSystem("STEM");
        logSystem.setFileLogger(new File("logs"));
        logSystem.setLogLevel(Level.ALL);
        LOGGER = logSystem.getLogger();
        STEMApp.LOGGER.CONFIG("""
                :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
                ::  ██████   ██████  ███                                 ██████   █████ ██████████ ███████████ ::
                :: ░░██████ ██████  ░░░                                 ░░██████ ░░███ ░░███░░░░░█░█░░░███░░░█ ::
                ::  ░███░█████░███  ████  ████████  ████████   ██████    ░███░███ ░███  ░███  █ ░ ░   ░███  ░  ::
                ::  ░███░░███ ░███ ░░███ ░░███░░███░░███░░███ ░░░░░███   ░███░░███░███  ░██████       ░███     ::
                ::  ░███ ░░░  ░███  ░███  ░███ ░░░  ░███ ░░░   ███████   ░███ ░░██████  ░███░░█       ░███     ::
                ::  ░███      ░███  ░███  ░███      ░███      ███░░███   ░███  ░░█████  ░███ ░   █    ░███     ::
                ::  █████     █████ █████ █████     █████    ░░████████  █████  ░░█████ ██████████    █████    ::
                :: ░░░░░     ░░░░░ ░░░░░ ░░░░░     ░░░░░      ░░░░░░░░  ░░░░░    ░░░░░ ░░░░░░░░░░    ░░░░░     ::
                :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
                                                                                                                 \s""");
        STEMApp.LOGGER.CORE(STEMApp.class.getSimpleName() + " loading MirraNET mainframe...");
        new STEMApp(args);
    }

    public static STEMApp getInstance() {
        return instance;
    }

    /**
     * Create a clean temp folder. Leftovers from a previous run (e.g. after a crash or kill -9,
     * when no shutdown hook ran) are removed first, so the folder never grows across restarts.
     */
    private void createTempFolder() {
        this.tempFolder = new File("_temp");
        deleteRecursively(this.tempFolder);
        if (!this.tempFolder.mkdirs() && !this.tempFolder.isDirectory()) {
            STEMApp.LOGGER.ERROR("Could not create temp folder " + this.tempFolder.getAbsolutePath());
        }
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        try (Stream<Path> paths = Files.walk(file.toPath())) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException _) {
                }
            });
        } catch (IOException _) {
        }
    }

    /**
     * Run a clean shutdown when the JVM is terminated (SIGTERM, service stop, System.exit)
     * and remove the temp folder afterwards.
     */
    private void registerShutdownHook() {
        Thread hook = new Thread(() -> {
            if (this.shutdownStarted.get()) {
                /* shutdown() already ran or is running: just wait for it to finish */
                this.awaitShutdown();
            } else {
                this.shutdown(false);
            }
            deleteRecursively(this.tempFolder);
        }, "STEM-ShutdownHook");
        Runtime.getRuntime().addShutdownHook(hook);
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

    /**
     * Shut down STEM and exit the JVM.
     */
    public void shutdown() {
        this.shutdown(true);
    }

    private void shutdown(boolean exitJvm) {
        if (!this.shutdownStarted.compareAndSet(false, true)) {
            STEMApp.LOGGER.WARNING("Shutdown already in progress");
            return;
        }
        try {
            /* Plugins first: they may still use core modules in onDisable */
            this.shutdownModule(this.pluginModule);
            this.shutdownModule(this.cloudModule);
            this.shutdownModule(this.commandModule);
            this.shutdownModule(this.scriptManager);
            this.shutdownModule(this.complianceModule);
            this.shutdownModule(this.healthModule);
            this.shutdownModule(this.informationModule);
            this.shutdownModule(this.notificationModule);
            this.shutdownModule(this.stemLinkModule);
            this.shutdownModule(this.mqttModule);
            this.shutdownModule(this.databaseModule);
            this.shutdownModule(this.libraryModule);
            this.shutdownModule(this.eventModule);
            this.stemKernel.endCore();
            this.isActive.set(false);
            STEMApp.LOGGER.CORE("Shutdown complete!");
        } finally {
            this.shutdownLatch.countDown();
        }
        if (exitJvm) {
            System.exit(0);
        }
    }

    private void shutdownModule(AbstractModule module) {
        if (module == null) {
            return;
        }
        try {
            module.shutdownModule();
        } catch (Throwable throwable) {
            STEMApp.LOGGER.ERROR("Error while unloading module " + module.getClass().getSimpleName() + ": " + throwable);
        }
    }

    private void awaitShutdown() {
        try {
            if (!this.shutdownLatch.await(60, TimeUnit.SECONDS)) {
                STEMApp.LOGGER.ERROR("Shutdown did not finish within 60 seconds");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

    public ComplianceModule getComplianceModule() {
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

    public File getTempFolder() {
        return tempFolder;
    }
}
