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
import de.linzn.stem.api.health.HealthService;
import de.linzn.stem.modules.healthModule.HealthCheck;

import java.util.List;

final class HealthServiceAdapter implements HealthService {
    private final STEMApp stemApp;

    HealthServiceAdapter(STEMApp stemApp) {
        this.stemApp = stemApp;
    }

    @Override
    public void register(HealthCheck check) {
        this.stemApp.getHealthModule().registerHealthCheck(check);
    }

    @Override
    public void unregister(HealthCheck check) {
        this.stemApp.getHealthModule().unregisterHealthCheck(check);
    }

    @Override
    public void runNow() {
        this.stemApp.getHealthModule().runCheckManual();
    }

    @Override
    public List<HealthCheck> getChecks() {
        return List.copyOf(this.stemApp.getHealthModule().getHealthChecks());
    }
}
