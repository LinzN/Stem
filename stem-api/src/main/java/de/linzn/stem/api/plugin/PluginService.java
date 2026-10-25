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

package de.linzn.stem.api.plugin;

import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.List;
import java.util.Optional;

/**
 * Information about loaded plugins.
 */
public interface PluginService {

    /**
     * @return all loaded plugins in load order
     */
    List<STEMPlugin> getPlugins();

    /**
     * @return the plugin with the given name
     */
    Optional<STEMPlugin> getPlugin(String name);

    /**
     * @return true if the plugin is loaded and enabled
     */
    boolean isEnabled(String name);
}
