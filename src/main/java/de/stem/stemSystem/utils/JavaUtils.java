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

package de.stem.stemSystem.utils;

import de.stem.stemSystem.STEMSystemApp;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.Properties;

public class JavaUtils {

    public static String getVersion() {
        String version;
        String res = "META-INF/maven/de.stem/stem-system/pom.properties";
        URL url = Thread.currentThread().getContextClassLoader().getResource(res);
        if (url == null) {
            version = "SS";
        } else {
            Properties props = new Properties();
            try {
                props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(res));
            } catch (IOException e) {
                STEMSystemApp.LOGGER.ERROR(e);
            }
            version = props.getProperty("version");
        }

        return version;
    }

    public static double getSystemLoad() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        return operatingSystemMXBean.getSystemLoadAverage();
    }

    public static int getCoreAmount() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        return operatingSystemMXBean.getAvailableProcessors();
    }

    public static double getUsedMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        return (memoryMXBean.getHeapMemoryUsage().getUsed() / (1000 * 1000));
    }

    public static double getMaxMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        return (memoryMXBean.getHeapMemoryUsage().getMax() / (1000 * 1000));
    }

    public static long getUsableSpace() {
        return new File("/").getUsableSpace();
    }

    public static long getTotalSpace() {
        return new File("/").getTotalSpace();
    }

    public static Instant getTimeInstant() {
        return Instant.now(Clock.systemDefaultZone());
    }
}
