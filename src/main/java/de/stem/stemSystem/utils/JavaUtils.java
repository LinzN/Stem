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
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

public class JavaUtils {

    public static String getVersion_old() {
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

    public static String getBuildNumber(){
        InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("version.yml");
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(inStream);
        return String.valueOf(obj.get("buildNumber"));
    }

}
