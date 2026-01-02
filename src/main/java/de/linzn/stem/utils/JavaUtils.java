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

package de.linzn.stem.utils;

import de.linzn.stem.STEMApp;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class JavaUtils {

    public static Map<String, Object> data;

    public static String getVersion() {
        readConfig();
        String kernelName = getKernelName();
        String versionNumber = getBuildVersion();
        String buildNumber = getBuildNumber();
        String buildLabel = getBuildLabel();
        return kernelName + "_" + versionNumber + "." + buildNumber + "-" + buildLabel;
    }

    public static String getKernelName() {
        readConfig();
        return String.valueOf(data.get("kernelName")).toUpperCase();
    }

    public static String getBuildVersion() {
        readConfig();
        return String.valueOf(data.get("buildVersion")).toUpperCase();
    }

    public static String getBuildNumber() {
        readConfig();
        return String.valueOf(data.get("buildNumber"));
    }

    public static String getBuildLabel() {
        readConfig();
        return String.valueOf(data.get("buildLabel"));
    }

    public static void readConfig() {
        if (data == null) {
            InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("version.yml");
            Yaml yaml = new Yaml();
            data = yaml.load(inStream);
        }
    }

    public static Path getFilePath() {
        try {
            return Paths.get(STEMApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            STEMApp.LOGGER.ERROR(e);
        }
        return null;
    }
}
