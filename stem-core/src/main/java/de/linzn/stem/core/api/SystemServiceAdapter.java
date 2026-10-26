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
import de.linzn.stem.api.system.LogEntry;
import de.linzn.stem.api.system.SystemService;
import de.linzn.stem.taskManagment.SchedulerService;
import de.linzn.stem.utils.JavaUtils;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

final class SystemServiceAdapter implements SystemService {
    private final STEMApp stemApp;

    SystemServiceAdapter(STEMApp stemApp) {
        this.stemApp = stemApp;
    }

    @Override
    public String getVersion() {
        return JavaUtils.getVersion();
    }

    @Override
    public Instant getStartTime() {
        return this.stemApp.getUptimeDate().toInstant();
    }

    @Override
    public Duration getUptime() {
        return Duration.between(this.getStartTime(), Instant.now());
    }

    @Override
    public ZoneId getZone() {
        return SchedulerService.STEM_ZONE;
    }

    @Override
    public File getTempFolder() {
        File tempFolder = this.stemApp.getTempFolder();
        if (tempFolder == null) {
            throw new IllegalStateException("Temp folder is not created yet");
        }
        return tempFolder;
    }

    @Override
    public boolean isActive() {
        return this.stemApp.isActive();
    }

    @Override
    public List<LogEntry> getRecentLog(int max, int minLevelValue) {
        List<LogEntry> entries = new ArrayList<>();
        if (max <= 0 || STEMApp.LOGGER == null) {
            return entries;
        }
        /* the logger keeps a bounded history; filter from the newest backwards */
        List<LogRecord> records = STEMApp.LOGGER.getLastEntries(Integer.MAX_VALUE);
        for (int i = records.size() - 1; i >= 0 && entries.size() < max; i--) {
            LogRecord record = records.get(i);
            if (record.getLevel() == null || record.getLevel().intValue() < minLevelValue) {
                continue;
            }
            String message = record.getMessage();
            if (record.getThrown() != null) {
                message = (message == null ? "" : message + " ") + record.getThrown();
            }
            entries.add(new LogEntry(record.getInstant(), record.getLevel().getName(), record.getLevel().intValue(), message));
        }
        java.util.Collections.reverse(entries);
        return entries;
    }
}
