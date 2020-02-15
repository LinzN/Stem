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

import de.azcore.azcoreRuntime.module.appConfiguration.AppConfigurationModule;
import de.azcore.azcoreRuntime.module.database.DatabaseModule;
import de.azcore.azcoreRuntime.module.network.NetworkModule;
import de.azcore.azcoreRuntime.module.notification.NotificationModule;
import de.azcore.azcoreRuntime.module.plugin.PluginModule;
import de.azcore.azcoreRuntime.module.terminal.TerminalModule;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AZCoreRuntimeApp {
    // The main instance
    private static AZCoreRuntimeApp instance;
    private static Logger fileLogger;
    private static AtomicBoolean verbose = new AtomicBoolean(false);

    private Heartbeat heartbeat;
    private AppConfigurationModule appConfigurationModule;
    private NetworkModule networkModule;
    private TerminalModule terminalModule;
    private NotificationModule notificationModule;
    private DatabaseModule databaseModule;
    private PluginModule pluginModule;
    private long start_time;

    public AZCoreRuntimeApp(String[] args) {
        this.start_time = System.nanoTime();
        instance = this;
        this.heartbeat = new Heartbeat(instance);
        Thread main = new Thread(this.heartbeat);
        main.setName("heartbeat");
        main.start();
        loadModules();
        finishStartup();
    }

    public static void main(String[] args) {
        logSetup();
        AZCoreRuntimeApp.logger(AZCoreRuntimeApp.class.getSimpleName() + " load mainframe...", true, false);
        new AZCoreRuntimeApp(args);
    }

    // The default fileLogger
    public static synchronized void logger(String log, boolean writeToFile, boolean debugInfo) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        if (!debugInfo || verbose.get()) {
            System.out.print(dateFormat.format(new Date().getTime()) + (debugInfo ? "[Debug]" : "") + " [" + Thread.currentThread().getName() + "] " + log + "\n");
            System.out.flush();
            if (writeToFile) {
                fileLogger.info(dateFormat.format(new Date().getTime()) + "[" + Thread.currentThread().getName() + "] " + log);
            }
        }
    }

    private static void logSetup() {
        fileLogger = Logger.getLogger("AZCore");
        fileLogger.setUseParentHandlers(false);
        FileHandler fh;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");

        try {
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdir();
            }

            fh = new FileHandler("logs/" + dateFormat.format(new Date().getTime()) + ".log");
            fileLogger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter() {
                private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format,
                            new Date(lr.getMillis()),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage()
                    );
                }
            };

            fh.setFormatter(formatter);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean getVerbose() {
        return verbose.get();
    }

    public static void setVerbose(boolean value) {
        verbose.set(value);
    }

    public static AZCoreRuntimeApp getInstance() {
        return instance;
    }

    // Load the modules for the framework
    private void loadModules() {
        this.heartbeat.runSyncTask(() -> appConfigurationModule = new AppConfigurationModule(instance));
        this.heartbeat.runSyncTask(() -> databaseModule = new DatabaseModule(instance));
        this.heartbeat.runSyncTask(() -> networkModule = new NetworkModule(instance));
        this.heartbeat.runSyncTask(() -> terminalModule = new TerminalModule(instance));
        this.heartbeat.runSyncTask(() -> notificationModule = new NotificationModule(instance));
        this.heartbeat.runSyncTask(() -> pluginModule = new PluginModule(instance));
    }

    private void finishStartup() {
        Runnable finish = () -> logger("AZCore-Runtime startup finished in " + (int) ((System.nanoTime() - start_time) / 1e6) + " ms.", true, false);
        this.heartbeat.runSyncTask(finish);
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

    public AppConfigurationModule getAppConfigurationModule() {
        return appConfigurationModule;
    }

    public NetworkModule getNetworkModule() {
        return networkModule;
    }

    public TerminalModule getTerminalModule() {
        return terminalModule;
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

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

}
