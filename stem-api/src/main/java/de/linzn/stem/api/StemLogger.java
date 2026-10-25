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

/**
 * Logger for plugins. Messages are prefixed with the owner's name.
 */
public interface StemLogger {

    void debug(String message);

    void info(String message);

    void warning(String message);

    void error(String message);

    void error(Throwable throwable);

    void error(String message, Throwable throwable);
}
