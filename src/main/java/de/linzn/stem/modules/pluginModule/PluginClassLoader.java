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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
            File zipFile = new File(pluginDirectory, "libraries.zip");
            if (zipFile.exists()) {
                try {
                    Path tempDir = plugin.getTempFolder().toPath();
                    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            if (entry.isDirectory()) continue;
                            Path newFile = tempDir.resolve(entry.getName());
                            Files.createDirectories(newFile.getParent()).toFile();
                            STEMApp.LOGGER.INFO("Copy temporarily library file " + newFile.toFile().getName() + " for plugin: " + plugin.getPluginName());
                            Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    // loading jar files
                    File dependencyDirectory = new File(tempDir.toFile(), "libraries");
                    if (dependencyDirectory.exists() && dependencyDirectory.isDirectory()) {
                        if (dependencyDirectory.exists() && dependencyDirectory.isDirectory()) {
                            File[] files = dependencyDirectory.listFiles();
                            for (File file : files) {
                                if (file.isFile()) {
                                    if (file.getName().endsWith(".jar")) {
                                        STEMApp.LOGGER.INFO("Loading temporarily library " + file.getName() + " for plugin: " + plugin.getPluginName());
                                        this.addURL(file.toURI().toURL());
                                    }
                                }
                            }
                        }
                    }

                } catch (IOException e) {
                    STEMApp.LOGGER.ERROR(e);
                }
            }
        }
    }
}
