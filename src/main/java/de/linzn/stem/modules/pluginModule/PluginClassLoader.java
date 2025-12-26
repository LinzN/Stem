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

package de.linzn.stem.modules.pluginModule;

import de.linzn.stem.STEMApp;

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
        STEMApp.LOGGER.INFO("Load plugin: " + pluginName);
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
            this.loadPluginLibraryFiles(plugin);
            return plugin;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InvalidPluginException("No public constructor");
        } catch (InstantiationException ex) {
            throw new InvalidPluginException("Abnormal plugin type");
        } catch (MalformedURLException e) {
            throw new InvalidPluginException("Plugin libraries not loaded");
        }
    }

    public synchronized void loadPluginLibraryFiles(STEMPlugin plugin) throws MalformedURLException {
        File pluginDirectory = plugin.getDataFolder();
        if (pluginDirectory.exists() && pluginDirectory.isDirectory()) {
            File dependencyDirectory = new File(pluginDirectory, "libraries");
            if (dependencyDirectory.exists() && dependencyDirectory.isDirectory()) {
                if (dependencyDirectory.exists() && dependencyDirectory.isDirectory()) {
                    File[] files = dependencyDirectory.listFiles();
                    STEMApp.LOGGER.INFO("Library directory found for plugin: " + plugin.getPluginName());
                    for (File file : files) {
                        if (file.isFile()) {
                            if (file.getName().endsWith(".jar")) {
                                STEMApp.LOGGER.INFO("Loading library " + file.getName() + " for plugin: " + plugin.getPluginName());
                                this.addURL(file.toURI().toURL());
                            }
                        }
                    }
                }
            }
        }
    }
}
