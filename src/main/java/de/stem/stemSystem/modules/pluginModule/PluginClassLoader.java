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

package de.stem.stemSystem.modules.pluginModule;

import de.stem.stemSystem.STEMSystemApp;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class PluginClassLoader extends URLClassLoader {

    public PluginClassLoader(ClassLoader parentClassLoader) {
        super(new URL[]{}, parentClassLoader);
    }

    public synchronized STEMPlugin addPluginFile(String pluginName, String classPath, String version, String buildJobName, String buildNumber, File jarFile) throws MalformedURLException {
        super.addURL(jarFile.toURI().toURL());
        return initPlugin(pluginName, classPath, version, buildJobName, buildNumber);
    }

    private STEMPlugin initPlugin(String pluginName, String classPath, String version, String buildJobName, String buildNumber) {
        STEMSystemApp.LOGGER.INFO("Load plugin: " + pluginName);
        try {
            Class<?> jarClass;
            try {
                jarClass = Class.forName(classPath, true, this);
            } catch (ClassNotFoundException ex) {
                throw new InvalidPluginException("Cannot find main class " + classPath + "'");
            }

            Class<? extends STEMPlugin> pluginClass;
            try {
                pluginClass = jarClass.asSubclass(STEMPlugin.class);
            } catch (ClassCastException ex) {
                throw new InvalidPluginException("Main class `" + classPath + "' does not extend Plugin");
            }
            STEMPlugin plugin = pluginClass.getDeclaredConstructor().newInstance();
            plugin.setUp(pluginName, version, buildJobName, buildNumber, classPath);
            return plugin;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InvalidPluginException("No public constructor");
        } catch (InstantiationException ex) {
            throw new InvalidPluginException("Abnormal plugin type");
        }
    }
}
