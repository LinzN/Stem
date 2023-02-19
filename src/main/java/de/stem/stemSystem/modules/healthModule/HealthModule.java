package de.stem.stemSystem.modules.healthModule;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import de.stem.stemSystem.modules.healthModule.test.DummyHealthCheck;
import de.stem.stemSystem.modules.informationModule.InformationBlock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class HealthModule extends AbstractModule {
    private final STEMSystemApp stemSystemApp;
    private final LinkedList<HealthCheck> healthChecks;
    private InformationBlock informationBlock;


    public HealthModule(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
        this.healthChecks = new LinkedList<>();
        this.startHealthModule();
        this.registerHealthCheck(new DummyHealthCheck());
    }

    public void registerHealthCheck(HealthCheck healthCheck) {
        this.healthChecks.add(healthCheck);
    }

    private void startHealthModule() {
        this.stemSystemApp.getScheduler().runTaskLater(this.getModulePlugin(), this::run, 2, TimeUnit.MINUTES);
        this.stemSystemApp.getScheduler().runFixedScheduler(this.getModulePlugin(), this::run, 0, 1, 0, true);
        this.stemSystemApp.getScheduler().runFixedScheduler(this.getModulePlugin(), this::run, 0, 7, 0, true);
        this.stemSystemApp.getScheduler().runFixedScheduler(this.getModulePlugin(), this::run, 0, 13, 0, true);
        this.stemSystemApp.getScheduler().runFixedScheduler(this.getModulePlugin(), this::run, 0, 19, 0, true);
    }


    @Override
    public void onShutdown() {
    }

    private void run() {
        if (this.informationBlock != null) {
            if (this.informationBlock.isActive()) {
                this.informationBlock.expire();
            }
        }

        STEMSystemApp.LOGGER.INFO("Starting system health check...");
        informationBlock = new InformationBlock("System Health Check", "Starting health check...", STEMSystemApp.getInstance().getScheduler().getDefaultSystemPlugin());
        informationBlock.setExpireTime(-1);
        informationBlock.setIcon("PROGRESS");
        STEMSystemApp.getInstance().getInformationModule().queueInformationBlock(informationBlock);


        for (int i = 0; i < healthChecks.size(); i++) {
            HealthCheck healthCheck = healthChecks.get(i);
            informationBlock.setDescription("Running check " + (i + 1) + " of " + healthChecks.size());
            STEMSystemApp.LOGGER.INFO("Running check " + (i + 1) + " of " + healthChecks.size());
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
        STEMSystemApp.LOGGER.INFO("Check done! Results -  OK:" + done + " Warnings:" + warning + " Errors: " + error);
    }
}
