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

package de.linzn.stem.api.health;

import de.linzn.stem.modules.healthModule.HealthCheck;

import java.util.List;

/**
 * System health checks. Checks run on the configured schedule or on demand.
 */
public interface HealthService {

    /**
     * Register a health check. Its owner is {@link HealthCheck#getStemPlugin()}.
     */
    void register(HealthCheck check);

    /**
     * Unregister a health check.
     */
    void unregister(HealthCheck check);

    /**
     * Run all health checks now (asynchronously).
     */
    void runNow();

    /**
     * @return all registered health checks
     */
    List<HealthCheck> getChecks();
}
