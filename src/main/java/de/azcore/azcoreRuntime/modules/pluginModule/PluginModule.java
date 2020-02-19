/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */
package de.azcore.azcoreRuntime.modules.pluginModule;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.modules.AbstractModule;
import de.azcore.azcoreRuntime.modules.pluginModule.loader.PluginClassLoader;
import de.linzn.openJL.pairs.Pair;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class PluginModule extends AbstractModule {
    public static File pluginDirectory = new File("plugins");
    private static String pluginFileName = "plugin.yml";
    private PluginClassLoader pluginClassLoader;
    private LinkedHashMap<String, AZPlugin> pluginList;
    private AZCoreRuntimeApp azCoreRuntime;

    public PluginModule(AZCoreRuntimeApp azCoreRuntime) {
        this.azCoreRuntime = azCoreRuntime;
        this.init();
        this.loadPlugins();
    }

    private void init() {
        this.pluginList = new LinkedHashMap<>();
        this.pluginClassLoader = new PluginClassLoader();

        if (!pluginDirectory.exists()) {
            pluginDirectory.mkdir();
        }
    }

    public void loadPlugin(File jarFile, boolean enablePlugin) throws IOException {
        URLClassLoader child = new URLClassLoader(new URL[]{jarFile.toURL()}, this.getClass().getClassLoader());
        InputStream inStream = child.getResourceAsStream(pluginFileName);
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(inStream);
        String pluginName = (String) obj.get("name");
        String classPath = (String) obj.get("main");
        String version = (String) obj.get("version");
        loadPlugin(pluginName, classPath, version, jarFile, enablePlugin);
    }

    private void loadPlugin(String pluginName, String classPath, String version, File jarFile, boolean enablePlugin) throws IOException {
        AZPlugin plugin = pluginClassLoader.addPluginFile(pluginName, classPath, version, jarFile);
        this.pluginList.put(plugin.getPluginName(), plugin);
        if (enablePlugin) {
            enablePlugin(plugin.getPluginName());
        }
    }

    public void unloadPlugin(String pluginName) {
        AZCoreRuntimeApp.logger("Unload plugin: " + pluginName, false, false);
        if (disablePlugin(pluginName)) {
            this.pluginList.remove(pluginName);
        }
    }

    public boolean enablePlugin(String pluginName) {
        AZPlugin plugin = this.pluginList.get(pluginName);
        if (plugin == null) {
            return false;
        }
        AZCoreRuntimeApp.logger("Enable plugin: " + plugin.getDescription(), false, false);

        try {
            plugin.onEnable();
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean disablePlugin(String pluginName) {
        AZPlugin plugin = this.pluginList.get(pluginName);
        if (plugin == null) {
            return false;
        }
        AZCoreRuntimeApp.logger("Disable plugin: " + plugin.getDescription(), false, false);
        try {
            plugin.onDisable();
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }

        return true;
    }

    private void unloadPlugins() {
        Set<String> pluginsCopy = new HashSet<>(this.pluginList.keySet());
        for (String name : pluginsCopy) {
            unloadPlugin(name);
        }
    }

    public void reloadPlugins() {
        this.unloadPlugins();
        this.init();
        this.loadPlugins();
    }

    private void loadPlugins() {
        LinkedList<Pair<String, File>> pluginsWithDependencies = new LinkedList<>();

        for (File jarFile : Objects.requireNonNull(pluginDirectory.listFiles())) {
            if (!jarFile.isDirectory() && jarFile.getName().endsWith(".jar")) {
                try {
                    URLClassLoader child = new URLClassLoader(new URL[]{jarFile.toURL()}, this.getClass().getClassLoader());

                    if (child.getResource(pluginFileName) == null) {
                        AZCoreRuntimeApp.logger("No " + pluginFileName + " file found for " + jarFile.getName(), true, false);
                        continue;
                    }

                    InputStream inStream = child.getResourceAsStream(pluginFileName);
                    Yaml yaml = new Yaml();
                    Map<String, Object> obj = yaml.load(inStream);
                    String pluginName = (String) obj.get("name");
                    String classPath = (String) obj.get("main");
                    String version = (String) obj.get("version");
                    List<String> dependencies = (List<String>) obj.get("depend");
                    if (dependencies == null || dependencies.size() == 0) {
                        loadPlugin(pluginName, classPath, version, jarFile, false);
                    } else {
                        pluginsWithDependencies.add(new Pair<>(pluginName, jarFile));
                    }
                    child.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }

        int i = 0;
        while (pluginsWithDependencies.size() != 0) {
            int maxValue = pluginsWithDependencies.size() * pluginsWithDependencies.size();

            if (i > maxValue) { //todo fix this
                AZCoreRuntimeApp.logger("Some plugins could not be loaded!", true, false);
                break;
            }

            try {
                Pair<String, File> pluginPair = pluginsWithDependencies.removeFirst();
                URLClassLoader child = new URLClassLoader(new URL[]{pluginPair.getValue().toURL()}, this.getClass().getClassLoader());
                InputStream inStream = child.getResourceAsStream(pluginFileName);
                Yaml yaml = new Yaml();
                Map<String, Object> obj = yaml.load(inStream);
                List<String> dependencies = (List<String>) obj.get("depend");

                for (String dependency : dependencies) {
                    if (!this.pluginList.containsKey(dependency)) {
                        pluginsWithDependencies.addLast(pluginPair);
                        break;
                    }
                }
                if (pluginsWithDependencies.size() == 0 || pluginsWithDependencies.getLast() != pluginPair) {
                    loadPlugin(pluginPair.getValue(), false);
                    i = 0;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            i++;
        }

        for (String pluginName : this.pluginList.keySet()) {
            enablePlugin(pluginName);
        }
    }

    public ArrayList<AZPlugin> getLoadedPlugins() {
        return new ArrayList<>(this.pluginList.values());
    }

}
