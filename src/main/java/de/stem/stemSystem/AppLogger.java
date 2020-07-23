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

import de.linzn.simplyLogger.LOGLEVEL;

import java.util.LinkedList;
import java.util.List;

public class AppLogger {

    private static final LinkedList<String> logEntries = new LinkedList<>();

    @Deprecated
    public static synchronized void logger(String log, boolean writeToFile) {
        if (writeToFile) {
            STEMSystemApp.LOGGER.INFO(log);
        } else {
            STEMSystemApp.LOGGER.LIVE(log);
        }
    }

    @Deprecated
    public static synchronized void debug(String log) {
        STEMSystemApp.LOGGER.DEBUG(log);
    }

    @Deprecated
    public static boolean getVerbose() {
        return STEMSystemApp.logSystem.getLogLevel() == LOGLEVEL.DEBUG;
    }

    @Deprecated
    public static void setVerbose(boolean value) {
        STEMSystemApp.logSystem.setLogLevel(value ? LOGLEVEL.DEBUG : LOGLEVEL.INFO);
    }



    @Deprecated
    public static List<String> getLastEntries(int max) {
        return STEMSystemApp.logSystem.getLastEntries(max);
    }

}
