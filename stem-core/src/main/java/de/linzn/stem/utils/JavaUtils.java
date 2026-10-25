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

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class JavaUtils {

    public static volatile Map<String, Object> data;

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

    public static synchronized void readConfig() {
        if (data == null) {
            try (InputStream inStream = JavaUtils.class.getClassLoader().getResourceAsStream("version.yml")) {
                Map<String, Object> loaded = inStream == null ? null : new Yaml().load(inStream);
                data = loaded != null ? loaded : new HashMap<>();
            } catch (IOException e) {
                data = new HashMap<>();
            }
        }
    }
}
