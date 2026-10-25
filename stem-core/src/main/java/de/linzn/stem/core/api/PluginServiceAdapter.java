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
import de.linzn.stem.api.plugin.PluginService;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.List;
import java.util.Optional;

final class PluginServiceAdapter implements PluginService {
    private final STEMApp stemApp;

    PluginServiceAdapter(STEMApp stemApp) {
        this.stemApp = stemApp;
    }

    @Override
    public List<STEMPlugin> getPlugins() {
        return List.copyOf(this.stemApp.getPluginModule().getLoadedPlugins());
    }

    @Override
    public Optional<STEMPlugin> getPlugin(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return this.getPlugins().stream().filter(plugin -> name.equalsIgnoreCase(plugin.getPluginName())).findFirst();
    }

    @Override
    public boolean isEnabled(String name) {
        return this.getPlugin(name).map(plugin -> this.stemApp.getPluginModule().isPluginEnabled(plugin.getPluginName())).orElse(false);
    }
}
