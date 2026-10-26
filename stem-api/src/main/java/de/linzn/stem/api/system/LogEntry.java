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

package de.linzn.stem.api.system;

import java.time.Instant;

/**
 * One line of the STEM log.
 *
 * @param time       time of the entry
 * @param level      level name, e.g. INFO, WARNING, SEVERE, ERROR
 * @param levelValue numeric level (java.util.logging): WARNING 900, SEVERE 1000
 * @param message    the message; plugin messages start with the plugin name
 */
public record LogEntry(Instant time, String level, int levelValue, String message) {
}
