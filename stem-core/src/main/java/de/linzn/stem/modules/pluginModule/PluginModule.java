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

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Loads plugins from the plugin directory at startup.
 * <p>
 * Plugins are loaded once, in dependency order, while STEM starts. Loading plugins at runtime is not
 * supported. When a plugin is disabled, every resource it registered in STEM (tasks, callbacks, event
 * listeners, MQTT subscriptions, commands, health checks, compliance checks, AI text engine) is removed.
 */
public class PluginModule extends AbstractModule {
    private static final String pluginFileName = "plugin.yml";
    public static File pluginDirectory = new File("plugins");
    private static volatile PluginModule instance;
    /* Plugin whose onEnable is currently running on this thread */
    private static final ThreadLocal<STEMPlugin> ENABLING_PLUGIN = new ThreadLocal<>();

    private final UpdateCheck updateCheck;
    private final Object pluginLock = new Object();
    /* Loaded plugins in load order (dependencies first). Guarded by pluginLock. */
    private final LinkedHashMap<String, STEMPlugin> pluginList = new LinkedHashMap<>();
    /* Names of enabled plugins. Guarded by pluginLock. */
    private final Set<String> enabledPlugins = new HashSet<>();
    public String jenkinsURL;
    private PluginClassLoader pluginClassLoader;
    private FileConfiguration fileConfiguration;
    private volatile boolean startupComplete = false;

    public PluginModule(STEMApp stemApp) {
        super(stemApp);
        instance = this;
        this.init();
        this.initConfig();
        this.loadPlugins();
        this.startupComplete = true;
        this.updateCheck = new UpdateCheck(this);
    }

    /**
     * Find the plugin a class belongs to.
     *
     * @param clazz class to check
     * @return owning plugin or null if the class belongs to the STEM core or a library
     */
    public static STEMPlugin findOwner(Class<?> clazz) {
        PluginModule pluginModule = instance;
        if (pluginModule == null || clazz == null) {
            return null;
        }
        return pluginModule.resolveOwner(clazz);
    }

    private void init() {
        this.pluginClassLoader = new PluginClassLoader(STEMApp.core().getStemClassLoader());

        if (!pluginDirectory.exists() && !pluginDirectory.mkdirs()) {
            STEMApp.LOGGER.ERROR("Could not create plugin directory " + pluginDirectory.getAbsolutePath());
        }
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_plugins.yml"));
        this.jenkinsURL = this.fileConfiguration.getString("jenkinsURL", "https://builds.mirranet.de/");
        this.fileConfiguration.save();
    }

    /**
     * Resolve the owner plugin of a class.
     * A registration done inside a plugin's onEnable belongs to that plugin. Otherwise the class must be
     * loaded by the plugin class loader and is matched to the plugin with the longest package prefix.
     *
     * @param clazz class to check
     * @return owning plugin or null
     */
    public STEMPlugin resolveOwner(Class<?> clazz) {
        STEMPlugin enabling = ENABLING_PLUGIN.get();
        if (enabling != null) {
            return enabling;
        }
        if (clazz == null || clazz.getClassLoader() != this.pluginClassLoader) {
            return null;
        }
        String className = clazz.getName();
        STEMPlugin bestMatch = null;
        int bestLength = -1;
        synchronized (this.pluginLock) {
            for (STEMPlugin plugin : this.pluginList.values()) {
                String mainClass = plugin.getClassPath();
                if (mainClass == null) {
                    continue;
                }
                int lastDot = mainClass.lastIndexOf('.');
                String prefix = lastDot > 0 ? mainClass.substring(0, lastDot + 1) : "";
                if (className.startsWith(prefix) && prefix.length() > bestLength) {
                    bestMatch = plugin;
                    bestLength = prefix.length();
                }
            }
        }
        return bestMatch;
    }

    /**
     * Load a plugin file. Only possible while STEM is starting.
     *
     * @param jarFile      plugin jar
     * @param enablePlugin enable the plugin after loading
     * @throws IOException if the plugin file can not be read
     */
    public void loadPlugin(File jarFile, boolean enablePlugin) throws IOException {
        if (this.startupComplete) {
            STEMApp.LOGGER.ERROR("Plugins can only be loaded at startup. Restart STEM to load " + jarFile.getName());
            return;
        }
        PluginDescriptor descriptor = readDescriptor(jarFile);
        this.loadPlugin(descriptor, enablePlugin);
    }

    private void loadPlugin(PluginDescriptor descriptor, boolean enablePlugin) throws IOException {
        synchronized (this.pluginLock) {
            if (this.pluginList.containsKey(descriptor.name)) {
                STEMApp.LOGGER.ERROR("Plugin " + descriptor.name + " is already loaded. Skipping " + descriptor.jarFile.getName());
                return;
            }
        }
        STEMPlugin plugin = pluginClassLoader.addPluginFile(descriptor.name, descriptor.mainClass, descriptor.version, descriptor.buildJobName, descriptor.buildNumber, descriptor.jarFile);
        synchronized (this.pluginLock) {
            this.pluginList.put(plugin.getPluginName(), plugin);
        }
        if (enablePlugin) {
            enablePlugin(plugin.getPluginName());
        }
    }

    private static PluginDescriptor readDescriptor(File jarFile) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            ZipEntry entry = jar.getEntry(pluginFileName);
            if (entry == null) {
                throw new InvalidPluginException("No " + pluginFileName + " file found for " + jarFile.getName());
            }
            Map<String, Object> obj;
            try (InputStream inStream = jar.getInputStream(entry)) {
                obj = new Yaml().load(inStream);
            }
            if (obj == null) {
                throw new InvalidPluginException("Empty " + pluginFileName + " in " + jarFile.getName());
            }
            String pluginName = obj.get("name") == null ? null : String.valueOf(obj.get("name"));
            String classPath = obj.get("main") == null ? null : String.valueOf(obj.get("main"));
            if (pluginName == null || pluginName.isBlank() || classPath == null || classPath.isBlank()) {
                throw new InvalidPluginException("Missing name or main in " + pluginFileName + " of " + jarFile.getName());
            }
            String version = String.valueOf(obj.get("version"));
            String buildJobName = obj.containsKey("buildJobName") ? String.valueOf(obj.get("buildJobName")) : "CUSTOM";
            String buildNumber = obj.containsKey("buildNumber") ? String.valueOf(obj.get("buildNumber")) : "SNAPSHOT";

            List<String> dependencies = new ArrayList<>();
            Object depend = obj.get("depend");
            if (depend instanceof Collection<?> collection) {
                for (Object dependency : collection) {
                    if (dependency != null) {
                        dependencies.add(String.valueOf(dependency));
                    }
                }
            } else if (depend != null) {
                dependencies.add(String.valueOf(depend));
            }
            return new PluginDescriptor(pluginName, classPath, version, buildJobName, buildNumber, List.copyOf(dependencies), jarFile);
        }
    }

    public void unloadPlugin(String pluginName) {
        STEMApp.LOGGER.INFO("Unload plugin: " + pluginName);
        disablePlugin(pluginName);
        STEMPlugin plugin;
        synchronized (this.pluginLock) {
            plugin = this.pluginList.remove(pluginName);
        }
        if (plugin != null && this.getStemApp().getApiProvider() != null) {
            this.getStemApp().getApiProvider().release(plugin);
        }
    }

    public boolean enablePlugin(String pluginName) {
        STEMPlugin plugin;
        synchronized (this.pluginLock) {
            plugin = this.pluginList.get(pluginName);
            if (plugin == null || this.enabledPlugins.contains(pluginName)) {
                return false;
            }
        }
        STEMApp.LOGGER.INFO("Enable plugin: " + plugin.getDescription());

        ENABLING_PLUGIN.set(plugin);
        try {
            plugin.onEnable();
        } catch (Throwable throwable) {
            STEMApp.LOGGER.ERROR("Failed to enable plugin " + pluginName + ": " + throwable);
            if (throwable instanceof Exception exception) {
                STEMApp.LOGGER.ERROR(exception);
            }
            /* Remove everything the plugin registered before it failed */
            this.cleanupPlugin(plugin);
            return false;
        } finally {
            ENABLING_PLUGIN.remove();
        }
        synchronized (this.pluginLock) {
            this.enabledPlugins.add(pluginName);
        }
        return true;
    }

    public boolean disablePlugin(String pluginName) {
        STEMPlugin plugin;
        synchronized (this.pluginLock) {
            plugin = this.pluginList.get(pluginName);
            if (plugin == null || !this.enabledPlugins.remove(pluginName)) {
                return false;
            }
        }
        STEMApp.LOGGER.INFO("Disable plugin: " + plugin.getDescription());
        boolean success = true;
        try {
            plugin.onDisable();
        } catch (Throwable throwable) {
            STEMApp.LOGGER.ERROR("Error while disabling plugin " + pluginName + ": " + throwable);
            if (throwable instanceof Exception exception) {
                STEMApp.LOGGER.ERROR(exception);
            }
            success = false;
        } finally {
            /* Always remove the plugin's resources, even if onDisable failed */
            this.cleanupPlugin(plugin);
        }
        return success;
    }

    /**
     * Remove every resource a plugin registered in the STEM core.
     *
     * @param plugin plugin to clean up
     */
    private void cleanupPlugin(STEMPlugin plugin) {
        STEMApp stemApp = this.getStemApp();
        this.safeCleanup(plugin, "callbacks", () -> stemApp.getCallBackService().unregisterCallbackListeners(plugin));
        this.safeCleanup(plugin, "tasks", () -> stemApp.getScheduler().cancelTasks(plugin));
        this.safeCleanup(plugin, "event listeners", () -> {
            if (stemApp.getEventModule() != null) {
                stemApp.getEventModule().getStemEventBus().unregisterAll(plugin);
            }
        });
        this.safeCleanup(plugin, "mqtt subscriptions", () -> {
            if (stemApp.getMqttModule() != null) {
                stemApp.getMqttModule().unsubscribeAll(plugin);
            }
        });
        this.safeCleanup(plugin, "commands", () -> {
            if (stemApp.getCommandModule() != null) {
                stemApp.getCommandModule().unregisterCommands(plugin);
            }
        });
        this.safeCleanup(plugin, "health checks", () -> {
            if (stemApp.getHealthModule() != null) {
                stemApp.getHealthModule().unregisterHealthChecks(plugin);
            }
        });
        this.safeCleanup(plugin, "compliance checks", () -> {
            if (stemApp.getComplianceModule() != null) {
                stemApp.getComplianceModule().unregisterComplianceChecks(plugin);
            }
        });
        this.safeCleanup(plugin, "ai text engine", () -> {
            if (stemApp.getInformationModule() != null) {
                stemApp.getInformationModule().unregisterAiTextEngine(plugin);
            }
        });
    }

    private void safeCleanup(STEMPlugin plugin, String what, Runnable cleanup) {
        try {
            cleanup.run();
        } catch (Throwable throwable) {
            STEMApp.LOGGER.ERROR("Error while removing " + what + " of plugin " + plugin.getPluginName() + ": " + throwable);
        }
    }

    /**
     * Unload all plugins in reverse load order, so plugins are disabled before their dependencies.
     */
    private void unloadPlugins() {
        List<String> names;
        synchronized (this.pluginLock) {
            names = new ArrayList<>(this.pluginList.keySet());
        }
        Collections.reverse(names);
        for (String name : names) {
            unloadPlugin(name);
        }
    }

    private void loadPlugins() {
        File[] files = pluginDirectory.listFiles();
        if (files == null) {
            STEMApp.LOGGER.ERROR("Could not read plugin directory " + pluginDirectory.getAbsolutePath());
            return;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));

        /* Read all descriptors first */
        Map<String, PluginDescriptor> pending = new LinkedHashMap<>();
        for (File jarFile : files) {
            if (jarFile.isDirectory() || !jarFile.getName().endsWith(".jar")) {
                continue;
            }
            try {
                PluginDescriptor descriptor = readDescriptor(jarFile);
                if (pending.containsKey(descriptor.name)) {
                    STEMApp.LOGGER.ERROR("Duplicate plugin name " + descriptor.name + " in " + jarFile.getName() + ". Skipping this file!");
                    continue;
                }
                pending.put(descriptor.name, descriptor);
            } catch (Exception e) {
                STEMApp.LOGGER.ERROR("Could not read plugin " + jarFile.getName() + ": " + e.getMessage());
            }
        }

        /* Load in dependency order: repeat until no plugin with satisfied dependencies is left */
        boolean progress = true;
        while (!pending.isEmpty() && progress) {
            progress = false;
            Iterator<PluginDescriptor> iterator = pending.values().iterator();
            while (iterator.hasNext()) {
                PluginDescriptor descriptor = iterator.next();
                boolean dependenciesLoaded;
                synchronized (this.pluginLock) {
                    dependenciesLoaded = this.pluginList.keySet().containsAll(descriptor.dependencies);
                }
                if (!dependenciesLoaded) {
                    continue;
                }
                iterator.remove();
                progress = true;
                try {
                    this.loadPlugin(descriptor, false);
                } catch (Throwable throwable) {
                    STEMApp.LOGGER.ERROR("Could not load plugin " + descriptor.name + ": " + throwable);
                }
            }
        }

        for (PluginDescriptor descriptor : pending.values()) {
            List<String> missing = new ArrayList<>(descriptor.dependencies);
            synchronized (this.pluginLock) {
                missing.removeAll(this.pluginList.keySet());
            }
            STEMApp.LOGGER.ERROR("Plugin " + descriptor.name + " could not be loaded. Missing or circular dependencies: " + missing);
        }

        List<String> names;
        synchronized (this.pluginLock) {
            names = new ArrayList<>(this.pluginList.keySet());
        }
        for (String pluginName : names) {
            enablePlugin(pluginName);
        }
    }

    public ArrayList<STEMPlugin> getLoadedPlugins() {
        synchronized (this.pluginLock) {
            return new ArrayList<>(this.pluginList.values());
        }
    }

    public boolean isPluginEnabled(String pluginName) {
        synchronized (this.pluginLock) {
            return this.enabledPlugins.contains(pluginName);
        }
    }

    @Override
    public void onShutdown() {
        this.unloadPlugins();
    }

    public UpdateCheck getUpdateCheck() {
        return updateCheck;
    }

    private record PluginDescriptor(String name, String mainClass, String version, String buildJobName,
                                    String buildNumber, List<String> dependencies, File jarFile) {
    }
}
