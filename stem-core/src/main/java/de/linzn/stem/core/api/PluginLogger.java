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
import de.linzn.stem.api.StemLogger;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

/**
 * Logger that prefixes messages with the plugin name and writes to the STEM log.
 */
final class PluginLogger implements StemLogger {
    private final STEMPlugin owner;

    PluginLogger(STEMPlugin owner) {
        this.owner = owner;
    }

    private String prefix(String message) {
        return "[" + owner.getPluginName() + "] " + message;
    }

    @Override
    public void debug(String message) {
        STEMApp.LOGGER.DEBUG(prefix(message));
    }

    @Override
    public void info(String message) {
        STEMApp.LOGGER.INFO(prefix(message));
    }

    @Override
    public void warning(String message) {
        STEMApp.LOGGER.WARNING(prefix(message));
    }

    @Override
    public void error(String message) {
        STEMApp.LOGGER.ERROR(prefix(message));
    }

    @Override
    public void error(Throwable throwable) {
        this.error(String.valueOf(throwable.getMessage()), throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        STEMApp.LOGGER.ERROR(prefix(message));
        if (throwable != null) {
            STEMApp.LOGGER.ERROR(throwable);
        }
    }
}
