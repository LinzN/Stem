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

package de.linzn.stem.modules.healthModule.test;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.healthModule.HealthCheck;
import de.linzn.stem.modules.healthModule.HealthCheckFeedback;
import de.linzn.stem.modules.healthModule.HealthCheckLevel;

public class DummyHealthCheck extends HealthCheck {

    public DummyHealthCheck() {
        super(STEMApp.getInstance().getScheduler().getDefaultSystemPlugin());
    }

    @Override
    protected void runCheckProgress() {
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        HealthCheckFeedback healthCheckFeedback = new HealthCheckFeedback(HealthCheckLevel.DONE, "Default check of health system");
        this.addHealthCheckFeedback(healthCheckFeedback);
    }
}
