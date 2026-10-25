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
import de.linzn.stem.utils.SafeFiles;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class PluginClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public PluginClassLoader(ClassLoader parentClassLoader) {
        super(new URL[]{}, parentClassLoader);
    }

    public synchronized STEMPlugin addPluginFile(String pluginName, String classPath, String version, String buildJobName, String buildNumber, File jarFile) throws MalformedURLException {
        super.addURL(jarFile.toURI().toURL());
        return initPlugin(pluginName, classPath, version, buildJobName, buildNumber, jarFile.toPath());
    }

    private STEMPlugin initPlugin(String pluginName, String classPath, String version, String buildJobName, String buildNumber, Path filePath) {
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
            plugin.setUp(pluginName, version, buildJobName, buildNumber, classPath, filePath, new File(PluginModule.pluginDirectory, pluginName));
            this.loadPluginLibraryFiles(plugin);
            return plugin;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InvalidPluginException("No public constructor");
        } catch (InstantiationException ex) {
            throw new InvalidPluginException("Abnormal plugin type");
        } catch (MalformedURLException e) {
            throw new InvalidPluginException("Plugin libraries not loaded");
        } catch (LinkageError e) {
            throw new InvalidPluginException("Plugin class could not be linked: " + e);
        }
    }

    public synchronized void loadPluginLibraryFiles(STEMPlugin plugin) throws MalformedURLException {
        File pluginDirectory = plugin.getDataFolder();
        if (pluginDirectory == null || !pluginDirectory.isDirectory()) {
            return;
        }
        File zipFile = new File(pluginDirectory, "libraries.zip");
        if (!zipFile.isFile()) {
            return;
        }
        File tempFolder = plugin.getTempFolder();
        if (tempFolder == null) {
            STEMApp.LOGGER.ERROR("No temp folder available for plugin " + plugin.getPluginName());
            return;
        }
        Path tempDir = tempFolder.toPath();
        try {
            SafeFiles.unzip(zipFile.toPath(), tempDir, newFile ->
                    STEMApp.LOGGER.INFO("Copy temporarily library file " + newFile.getFileName() + " for plugin: " + plugin.getPluginName()));
        } catch (IOException e) {
            STEMApp.LOGGER.ERROR(e);
            return;
        }
        // loading jar files
        File dependencyDirectory = new File(tempDir.toFile(), "libraries");
        File[] files = dependencyDirectory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                STEMApp.LOGGER.INFO("Loading temporarily library " + file.getName() + " for plugin: " + plugin.getPluginName());
                this.addURL(file.toURI().toURL());
            }
        }
    }
}
