package de.stem.stemSystem.modules.informationModule;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InformationBlock {
    private final STEMPlugin sourcePlugin;
    private final List<InformationIntent> informationIntents;
    private final String name;
    private String description;
    private String longDescription;
    private boolean disableLongDescription;
    private final long creationTime;
    private long expireTime;
    private String icon;
    private long id;

    public InformationBlock(String name, String description, STEMPlugin sourcePlugin) {
        this(name, description, sourcePlugin, null, false);
    }

    public InformationBlock(String name, String description, STEMPlugin sourcePlugin, String eventText) {
        this(name, description, sourcePlugin, eventText, false);
    }

    public InformationBlock(String name, String description, STEMPlugin sourcePlugin, String eventText, boolean disableLongDescription) {
        this.creationTime = new Date().getTime();
        this.name = name;
        this.sourcePlugin = sourcePlugin;
        this.disableLongDescription = disableLongDescription;
        this.expireTime = 0;
        this.icon = "NONE";
        this.informationIntents = new ArrayList<>();
        this.setDescription(description, eventText);
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
        this.setDescription(description, null);
    }

    public void setDescription(String description, String eventText) {
        this.description = description;
        if (!this.disableLongDescription) {
            if (eventText != null) {
                this.longDescription = STEMSystemApp.getInstance().getInformationModule().runAiTextEngine(eventText);
            } else {
                this.longDescription = STEMSystemApp.getInstance().getInformationModule().runAiTextEngine(description);
            }
        } else {
            this.longDescription = description;
        }
    }

    public void disableLongDescription(boolean disableLongDescription) {
        this.disableLongDescription = disableLongDescription;
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

    public void addIntent(InformationIntent informationIntent) {
        this.informationIntents.add(informationIntent);
    }

    public boolean hasIntent(InformationIntent informationIntent) {
        return this.informationIntents.contains(informationIntent);
    }

    public void removeIntent(InformationIntent informationIntent) {
        this.informationIntents.remove(informationIntent);
    }

    public String getLongDescription() {
        return longDescription;
    }
}
