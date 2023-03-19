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
            this.loadPluginLibraryFiles(pluginName);

            STEMPlugin plugin = pluginClass.getDeclaredConstructor().newInstance();
            plugin.setUp(pluginName, version, buildJobName, buildNumber, classPath);
            return plugin;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InvalidPluginException("No public constructor");
        } catch (InstantiationException ex) {
            throw new InvalidPluginException("Abnormal plugin type");
        } catch (MalformedURLException e) {
            throw new InvalidPluginException("Plugin libraries not loaded");
        }
    }

    public synchronized void loadPluginLibraryFiles(String pluginName) throws MalformedURLException {
        File pluginDirectory = new File(PluginModule.pluginDirectory, pluginName);
        if (pluginDirectory.exists() && pluginDirectory.isDirectory()) {
            File dependencyDirectory = new File(pluginDirectory, "libraries");
            if (dependencyDirectory.exists() && dependencyDirectory.isDirectory()) {
                if (dependencyDirectory.exists() && dependencyDirectory.isDirectory()) {
                    File[] files = dependencyDirectory.listFiles();
                    STEMSystemApp.LOGGER.INFO("Library directory found for plugin: " + pluginName);
                    for (File file : files) {
                        if (file.isFile()) {
                            if (file.getName().endsWith(".jar")) {
                                STEMSystemApp.LOGGER.INFO("Loading library " + file.getName() + " for plugin: " + pluginName);
                                this.addJarFileToPlugin(file);
                            }
                        }
                    }
                }
            }
        }
    }


    public synchronized void addJarFileToPlugin(File jarFile) throws MalformedURLException {
        this.addURL(jarFile.toURI().toURL());
        STEMSystemApp.LOGGER.DEBUG("LOAD PLUGIN LIBRARY FILE: " + jarFile.getName());
    }
}
