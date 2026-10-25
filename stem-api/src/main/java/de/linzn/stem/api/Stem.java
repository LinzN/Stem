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

package de.linzn.stem.api;

import de.linzn.stem.modules.pluginModule.STEMPlugin;

/**
 * Static entry point to the STEM plugin API.
 * <p>
 * Plugins normally use {@link STEMPlugin#getStemApi()}, which returns an API bound to the plugin.
 * Everything registered through a bound API (tasks, listeners, commands, MQTT subscriptions, ...) is
 * owned by that plugin and removed automatically when the plugin is disabled.
 */
public final class Stem {
    private static volatile StemApiProvider provider;

    private Stem() {
    }

    /**
     * @return API bound to the STEM system itself
     * @throws IllegalStateException if STEM is not running
     */
    public static StemApi api() {
        return requireProvider().system();
    }

    /**
     * @param plugin plugin the API is bound to
     * @return API bound to the given plugin
     * @throws IllegalStateException if STEM is not running
     */
    public static StemApi api(STEMPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin must not be null");
        }
        return requireProvider().forPlugin(plugin);
    }

    /**
     * @return true if the STEM runtime has registered its API implementation
     */
    public static boolean isAvailable() {
        return provider != null;
    }

    /**
     * Internal: called once by the STEM runtime at startup. Not for plugin use.
     *
     * @param apiProvider API implementation
     */
    public static synchronized void registerProvider(StemApiProvider apiProvider) {
        if (provider != null) {
            throw new IllegalStateException("STEM API provider is already registered");
        }
        provider = apiProvider;
    }

    private static StemApiProvider requireProvider() {
        StemApiProvider current = provider;
        if (current == null) {
            throw new IllegalStateException("STEM runtime is not available");
        }
        return current;
    }
}
