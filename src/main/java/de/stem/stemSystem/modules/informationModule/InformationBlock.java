package de.stem.stemSystem.modules.informationModule;

import de.stem.stemSystem.modules.pluginModule.STEMPlugin;

import java.util.Date;

public class InformationBlock {

    private long creationTime;
    private final STEMPlugin sourcePlugin;
    private final String name;
    private final String description;
    private long expireTime;

    private long id;

    public InformationBlock(String name, String description, STEMPlugin sourcePlugin){
        this.creationTime = new Date().getTime();
        this.name = name;
        this.description = description;
        this.sourcePlugin = sourcePlugin;
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

    public long getId() {
        return id;
    }
    void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive(){
        long currentTime = new Date().getTime();
        return this.expireTime >= currentTime || this.expireTime == -1;
    }

    public void expire(){
        this.expireTime = 0;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
