/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.stem.modules.healthModule;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.healthModule.test.DummyHealthCheck;
import de.linzn.stem.modules.informationModule.InformationBlock;
import de.linzn.stem.modules.informationModule.InformationIntent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class HealthModule extends AbstractModule {
    private final STEMApp stemApp;
    private final LinkedList<HealthCheck> healthChecks;
    private InformationBlock informationBlock;
    private boolean blocked = false;


    public HealthModule(STEMApp stemApp) {
        this.stemApp = stemApp;
        this.healthChecks = new LinkedList<>();
        this.startHealthModule();
        this.registerHealthCheck(new DummyHealthCheck());
    }

    public void registerHealthCheck(HealthCheck healthCheck) {
        this.healthChecks.add(healthCheck);
    }

    private void startHealthModule() {
        this.stemApp.getScheduler().runTaskLater(this.getModulePlugin(), this::run, 2, TimeUnit.MINUTES);
        this.stemApp.getScheduler().runAsCronTask(this.getModulePlugin(), this::run, this.stemApp.getConfiguration().healthCheckCronjob);
    }


    @Override
    public void onShutdown() {
    }

    private void run() {
        if (!this.blocked) {
            this.blocked = true;
            if (this.informationBlock != null) {
                if (this.informationBlock.isActive()) {
                    this.informationBlock.expire();
                }
            }

            STEMApp.LOGGER.INFO("Starting system health check...");
            informationBlock = new InformationBlock("System Health Check", "Starting health check...", STEMApp.getInstance().getScheduler().getDefaultSystemPlugin());
            informationBlock.setExpireTime(-1);
            informationBlock.setIcon("PROGRESS");
            informationBlock.addIntent(InformationIntent.SHOW_DISPLAY);
            STEMApp.getInstance().getInformationModule().queueInformationBlock(informationBlock);


            for (int i = 0; i < healthChecks.size(); i++) {
                HealthCheck healthCheck = healthChecks.get(i);
                informationBlock.setDescription("Running check " + healthCheck.getName() + " " + (i + 1) + " of " + healthChecks.size());
                STEMApp.LOGGER.INFO("Running check " + healthCheck.getName() + " " + (i + 1) + " of " + healthChecks.size());
                healthCheck.runCheck();
            }

            int warning = 0;
            int error = 0;
            int done = 0;

            for (HealthCheck healthCheck : this.healthChecks) {
                for (HealthCheckFeedback healthCheckFeedback : healthCheck.getHealthCheckFeedbacks()) {
                    if (healthCheckFeedback.getHealthCheckLevel() == HealthCheckLevel.DONE) {
                        done++;
                    } else if (healthCheckFeedback.getHealthCheckLevel() == HealthCheckLevel.WARNING) {
                        warning++;
                    } else if (healthCheckFeedback.getHealthCheckLevel() == HealthCheckLevel.ERROR) {
                        error++;
                    }
                }
            }

            if (error != 0) {
                informationBlock.setExpireTime(-1);
                informationBlock.setIcon("ERROR");
            } else if (warning != 0) {
                informationBlock.setExpireTime(-1);
                informationBlock.setIcon("WARNING");
            } else {
                informationBlock.setExpireTime(Instant.now().plus(20, ChronoUnit.MINUTES));
                informationBlock.setIcon("SUCCESS");
            }

            informationBlock.setDescription("Check done! Results -  OK:" + done + " Warnings:" + warning + " Errors: " + error);
            STEMApp.LOGGER.INFO("Check done! Results -  OK:" + done + " Warnings:" + warning + " Errors: " + error);
            this.blocked = false;
        } else {
            STEMApp.LOGGER.ERROR("Not possible to start another health check. Still running!");
        }
    }

    public void runCheckManual() {
        this.stemApp.getScheduler().runTask(this.getModulePlugin(), this::run);
    }

    public ArrayList<HealthCheck> getHealthChecks() {
        return new ArrayList<>(this.healthChecks);
    }
}
