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

import de.azcore.azcoreRuntime.utils.Color;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class AppLogger {

    private static java.util.logging.Logger fileLogger;
    private static AtomicBoolean verbose;

    static {
        verbose = new AtomicBoolean(false);
        fileLogger = java.util.logging.Logger.getLogger("AZCore");
        fileLogger.setUseParentHandlers(false);
        FileHandler fh;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH.mm.ss");

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

    public static synchronized void logger(String log, boolean writeToFile) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        System.out.print(dateFormat.format(new Date().getTime()) + " [" + Thread.currentThread().getName() + "] " + log + "\n");
        System.out.flush();
        if (writeToFile) {
            fileLogger.info(dateFormat.format(new Date().getTime()) + "[" + Thread.currentThread().getName() + "] " + log);
        }
    }

    public static synchronized void debug(String log) {
        if (verbose.get()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            System.out.print(dateFormat.format(new Date().getTime()) + Color.YELLOW + " [" + Thread.currentThread().getName() + "] " + log + Color.RESET + "\n");
            System.out.flush();
        }
    }

    public static boolean getVerbose() {
        return verbose.get();
    }

    public static void setVerbose(boolean value) {
        verbose.set(value);
    }

}
