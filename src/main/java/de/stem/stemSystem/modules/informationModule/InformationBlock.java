package de.stem.stemSystem.modules.informationModule;

import de.stem.stemSystem.modules.pluginModule.STEMPlugin;

import java.time.Instant;
import java.util.Date;

public class InformationBlock {

    private final STEMPlugin sourcePlugin;
    private final String name;
    private String description;
    private long creationTime;
    private long expireTime;

    private String icon;

    private long id;

    public InformationBlock(String name, String description, STEMPlugin sourcePlugin) {
        this.creationTime = new Date().getTime();
        this.name = name;
        this.description = description;
        this.sourcePlugin = sourcePlugin;
        this.expireTime = 0;
        this.icon = "NONE";
    }

    public STEMPlugin getSourcePlugin() {
        return sourcePlugin;
    }

    public String getName() {
        return name;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public void setExpireTime(Instant expireTime) {
        this.expireTime = expireTime.toEpochMilli();
    }

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        long currentTime = new Date().getTime();
        return this.expireTime >= currentTime || this.expireTime == -1;
    }

    public void expire() {
        this.expireTime = 0;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
