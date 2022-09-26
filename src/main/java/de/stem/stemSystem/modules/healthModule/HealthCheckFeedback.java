package de.stem.stemSystem.modules.healthModule;

public class HealthCheckFeedback {

    private final HealthCheckLevel healthCheckLevel;
    private final String description;

    public HealthCheckFeedback(HealthCheckLevel healthCheckLevel, String description){
    this.healthCheckLevel = healthCheckLevel;
    this.description = description;
    }

    public HealthCheckLevel getHealthCheckLevel() {
        return healthCheckLevel;
    }

    public String getDescription() {
        return description;
    }
}
