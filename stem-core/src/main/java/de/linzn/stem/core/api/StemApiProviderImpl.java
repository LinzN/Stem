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

package de.linzn.stem.core.api;

import de.linzn.stem.STEMApp;
import de.linzn.stem.api.StemApi;
import de.linzn.stem.api.StemApiProvider;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates one {@link StemApiImpl} per plugin. The instance for a plugin is released when the plugin is unloaded.
 */
public final class StemApiProviderImpl implements StemApiProvider {
    private final STEMApp stemApp;
    private final ConcurrentHashMap<STEMPlugin, StemApiImpl> apis = new ConcurrentHashMap<>();

    public StemApiProviderImpl(STEMApp stemApp) {
        this.stemApp = stemApp;
    }

    @Override
    public StemApi system() {
        return this.forPlugin(this.stemApp.getScheduler().getDefaultSystemPlugin());
    }

    @Override
    public StemApi forPlugin(STEMPlugin plugin) {
        return this.apis.computeIfAbsent(plugin, owner -> new StemApiImpl(this.stemApp, owner));
    }

    /**
     * Drop the cached API instance of an unloaded plugin.
     *
     * @param plugin unloaded plugin
     */
    public void release(STEMPlugin plugin) {
        this.apis.remove(plugin);
    }
}
