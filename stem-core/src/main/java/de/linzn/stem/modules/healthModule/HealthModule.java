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

package de.linzn.stem.modules.healthModule;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.healthModule.test.DummyHealthCheck;
import de.linzn.stem.modules.informationModule.InformationBlock;
import de.linzn.stem.modules.informationModule.InformationIntent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class HealthModule extends AbstractModule {
    private final CopyOnWriteArrayList<HealthCheck> healthChecks;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile InformationBlock informationBlock;

    public HealthModule(STEMApp stemApp) {
        super(stemApp);
        this.healthChecks = new CopyOnWriteArrayList<>();
        this.startHealthModule();
        this.registerHealthCheck(new DummyHealthCheck());
    }

    public void registerHealthCheck(HealthCheck healthCheck) {
        if (healthCheck != null) {
            this.healthChecks.addIfAbsent(healthCheck);
        }
    }

    public void unregisterHealthCheck(HealthCheck healthCheck) {
        this.healthChecks.remove(healthCheck);
    }

    /**
     * Unregister all health checks of a plugin
     *
     * @param stemPlugin plugin to remove the health checks for
     */
    public void unregisterHealthChecks(STEMPlugin stemPlugin) {
        this.healthChecks.removeIf(healthCheck -> healthCheck.getStemPlugin() == stemPlugin);
    }

    private void startHealthModule() {
        this.getStemApp().getScheduler().runTaskLater(this.getModulePlugin(), this::run, 2, TimeUnit.MINUTES);
        this.getStemApp().getScheduler().runAsCronTask(this.getModulePlugin(), this::run, this.getStemApp().getConfiguration().healthCheckCronjob);
    }

    @Override
    public void onShutdown() {
    }

    private void run() {
        if (!this.running.compareAndSet(false, true)) {
            STEMApp.LOGGER.ERROR("Not possible to start another health check. Still running!");
            return;
        }
        try {
            this.runChecks();
        } finally {
            this.running.set(false);
        }
    }

    private void runChecks() {
        InformationBlock previousBlock = this.informationBlock;
        if (previousBlock != null && previousBlock.isActive()) {
            previousBlock.expire();
        }

        STEMApp.LOGGER.INFO("Starting system health check...");
        InformationBlock block = new InformationBlock("System Health Check", "Starting health check...", STEMApp.core().getScheduler().getDefaultSystemPlugin());
        block.setExpireTime(-1);
        block.setIcon("PROGRESS");
        block.addIntent(InformationIntent.SHOW_DISPLAY);
        this.informationBlock = block;
        STEMApp.core().getInformationModule().queueInformationBlock(block);

        List<HealthCheck> checks = new ArrayList<>(this.healthChecks);
        int warning = 0;
        int error = 0;
        int done = 0;

        for (int i = 0; i < checks.size(); i++) {
            HealthCheck healthCheck = checks.get(i);
            String progress = "Running check " + healthCheck.getName() + " " + (i + 1) + " of " + checks.size();
            block.setDescription(progress);
            STEMApp.LOGGER.INFO(progress);
            try {
                healthCheck.runCheck();
            } catch (Throwable throwable) {
                STEMApp.LOGGER.ERROR("Health check " + healthCheck.getName() + " failed: " + throwable);
                healthCheck.addHealthCheckFeedback(new HealthCheckFeedback(HealthCheckLevel.ERROR, "Check crashed: " + throwable));
            }
            for (HealthCheckFeedback healthCheckFeedback : new ArrayList<>(healthCheck.getHealthCheckFeedbacks())) {
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
            block.setExpireTime(-1);
            block.setIcon("ERROR");
        } else if (warning != 0) {
            block.setExpireTime(-1);
            block.setIcon("WARNING");
        } else {
            block.setExpireTime(Instant.now().plus(20, ChronoUnit.MINUTES));
            block.setIcon("SUCCESS");
        }
        String result = "Check done! Results -  OK:" + done + " Warnings:" + warning + " Errors: " + error;
        block.setDescription(result);
        STEMApp.LOGGER.INFO(result);
    }

    public void runCheckManual() {
        this.getStemApp().getScheduler().runTask(this.getModulePlugin(), this::run);
    }

    public ArrayList<HealthCheck> getHealthChecks() {
        return new ArrayList<>(this.healthChecks);
    }
}
