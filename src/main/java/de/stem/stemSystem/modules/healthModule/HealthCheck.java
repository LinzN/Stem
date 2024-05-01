package de.stem.stemSystem.modules.healthModule;

import de.stem.stemSystem.modules.pluginModule.STEMPlugin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class HealthCheck {

    private final STEMPlugin stemPlugin;
    private LinkedList<HealthCheckFeedback> healthCheckFeedbacks;

    private String name;

    public HealthCheck(STEMPlugin stemPlugin) {
        this.stemPlugin = stemPlugin;
        healthCheckFeedbacks = new LinkedList<>();
        this.name = stemPlugin.getPluginName().toUpperCase();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    public List<HealthCheckFeedback> getHealthCheckFeedbacks(HealthCheckLevel level) {
        List<HealthCheckFeedback> list = new ArrayList<>();
        for (HealthCheckFeedback healthCheckFeedback : healthCheckFeedbacks) {
            if (healthCheckFeedback.getHealthCheckLevel() == level) {
                list.add(healthCheckFeedback);
            }
        }
        return list;
    }
}
