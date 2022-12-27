package de.stem.stemSystem.modules.healthModule;

import de.stem.stemSystem.modules.pluginModule.STEMPlugin;

import java.util.LinkedList;

public abstract class HealthCheck {

    private final STEMPlugin stemPlugin;
    private LinkedList<HealthCheckFeedback> healthCheckFeedbacks;

    public HealthCheck(STEMPlugin stemPlugin) {
        this.stemPlugin = stemPlugin;
        healthCheckFeedbacks = new LinkedList<>();
    }

    public void runCheck() {
        this.healthCheckFeedbacks = new LinkedList<>();
        this.runCheckProgress();
    }

    protected abstract void runCheckProgress();

    public void addHealthCheckFeedback(HealthCheckFeedback healthCheckFeedback) {
        this.healthCheckFeedbacks.add(healthCheckFeedback);
    }

    public LinkedList<HealthCheckFeedback> getHealthCheckFeedbacks() {
        return healthCheckFeedbacks;
    }

    public STEMPlugin getStemPlugin() {
        return stemPlugin;
    }
}
