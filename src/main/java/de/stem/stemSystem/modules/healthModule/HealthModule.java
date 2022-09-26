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
        this.stemSystemApp.getScheduler().runRepeatScheduler(this.getModulePlugin(), this::run, 2, 720, TimeUnit.MINUTES);
    }


    @Override
    public void onShutdown() {
    }

    private void run() {
        InformationBlock informationBlock = new InformationBlock("System Heath Check", "Starting health check...", STEMSystemApp.getInstance().getScheduler().getDefaultSystemPlugin());
        informationBlock.setExpireTime(Instant.now().plus(1, ChronoUnit.HOURS));
        STEMSystemApp.getInstance().getInformationModule().queueInformationBlock(informationBlock);


        for (int i = 0; i < healthChecks.size(); i++) {
            HealthCheck healthCheck = healthChecks.get(i);
            informationBlock.setDescription("Running check " + (i + 1) + " of " + healthChecks.size());
            healthCheck.runCheckProgress();

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
            informationBlock.setExpireTime(Instant.now().plus(5, ChronoUnit.HOURS));
        } else if (warning != 0) {
            informationBlock.setExpireTime(Instant.now().plus(1, ChronoUnit.HOURS));
        } else {
            informationBlock.setExpireTime(Instant.now().plus(15, ChronoUnit.MINUTES));
        }

        informationBlock.setDescription("Check done! Results -  Done:" + done + " Warnings:" + warning + " Errors: " + error);
    }
}
