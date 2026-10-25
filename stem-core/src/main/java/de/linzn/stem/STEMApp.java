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
import de.linzn.stem.api.Stem;
import de.linzn.stem.configuration.AppConfiguration;
import de.linzn.stem.core.api.StemApiProviderImpl;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * STEM runtime: owns the kernel and all core modules.
 * <p>
 * This class is internal. Plugins should use the API from the stem-api artifact via
 * {@link de.linzn.stem.modules.pluginModule.STEMPlugin#getStemApi()}; the getters here remain for existing plugins.
 */
public class STEMApp {

    public static Logger LOGGER;
    public static LogSystem logSystem;
    private static volatile STEMApp instance;
    private final AtomicBoolean isActive;
    private final AtomicBoolean shutdownStarted;
    private final List<AbstractModule> loadedModules = new CopyOnWriteArrayList<>();
    private final StemApiProviderImpl apiProvider;
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
        this.apiProvider = new StemApiProviderImpl(this);
        Stem.registerProvider(this.apiProvider);
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

    /**
     * Legacy entry point for plugins.
     *
     * @return the running STEM instance
     * @deprecated Plugins should use {@link de.linzn.stem.modules.pluginModule.STEMPlugin#getStemApi()}.
     * This method stays available for existing plugins.
     */
    @Deprecated(since = "1.2.0")
    public static STEMApp getInstance() {
        return instance;
    }

    /**
     * Internal accessor for STEM core code. Plugins use {@link de.linzn.stem.modules.pluginModule.STEMPlugin#getStemApi()}.
     *
     * @return the running STEM instance
     */
    public static STEMApp core() {
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

    /**
     * Load all core modules in dependency order. Plugins are loaded last, so every core module
     * is available when a plugin is enabled. Modules are unloaded in reverse order.
     */
    private void loadModules() {
        appConfiguration = new AppConfiguration(instance);
        eventModule = this.load(new EventModule(instance));
        databaseModule = this.load(new DatabaseModule(instance));
        stemLinkModule = this.load(new StemLinkModule(instance));
        mqttModule = this.load(new MqttModule(instance));
        notificationModule = this.load(new NotificationModule(instance));
        informationModule = this.load(new InformationModule(instance));
        scriptManager = this.load(new ScriptManager(instance));
        complianceModule = this.load(new ComplianceModule(instance));
        healthModule = this.load(new HealthModule(instance));
        commandModule = this.load(new CommandModule(instance));
        cloudModule = this.load(new CloudModule(instance));
        libraryModule = this.load(new LibraryModule(instance));
        pluginModule = this.load(new PluginModule(instance));
    }

    private <T extends AbstractModule> T load(T module) {
        this.loadedModules.add(module);
        return module;
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
            /* Reverse load order: plugins first, they may still use core modules in onDisable */
            List<AbstractModule> modules = new ArrayList<>(this.loadedModules);
            Collections.reverse(modules);
            for (AbstractModule module : modules) {
                this.shutdownModule(module);
            }
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

    /**
     * @return implementation of the plugin API
     */
    public StemApiProviderImpl getApiProvider() {
        return this.apiProvider;
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
