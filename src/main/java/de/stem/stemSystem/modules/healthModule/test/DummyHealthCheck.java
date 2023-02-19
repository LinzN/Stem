package de.stem.stemSystem.modules.healthModule.test;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.healthModule.HealthCheck;
import de.stem.stemSystem.modules.healthModule.HealthCheckFeedback;
import de.stem.stemSystem.modules.healthModule.HealthCheckLevel;

public class DummyHealthCheck extends HealthCheck {

    public DummyHealthCheck() {
        super(STEMSystemApp.getInstance().getScheduler().getDefaultSystemPlugin());
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
