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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

public class JavaUtils {

    public static String getVersion() {
        InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("version.yml");
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(inStream);
        String kernelName = String.valueOf(obj.get("kernelName"));
        String versionNumber = String.valueOf(obj.get("buildVersion"));
        String buildNumber = String.valueOf(obj.get("buildNumber"));
        String buildLabel = String.valueOf(obj.get("buildLabel"));
        return kernelName.toUpperCase() + "_" + versionNumber + "." + buildNumber + "-" + buildLabel;
    }

    public static String getKernelName(){
        InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("version.yml");
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(inStream);
        String kernelName = String.valueOf(obj.get("kernelName"));
        return  kernelName.toUpperCase();
    }

    public static String getBuildNumber(){
        InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("version.yml");
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(inStream);
        return String.valueOf(obj.get("buildNumber"));
    }

}
