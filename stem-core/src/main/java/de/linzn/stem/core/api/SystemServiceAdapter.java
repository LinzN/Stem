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
import de.linzn.stem.api.system.SystemService;
import de.linzn.stem.taskManagment.SchedulerService;
import de.linzn.stem.utils.JavaUtils;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

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
}
