package de.stem.stemSystem.modules.informationModule.listener;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.eventModule.handler.StemEventHandler;
import de.stem.stemSystem.modules.informationModule.InformationBlock;
import de.stem.stemSystem.modules.informationModule.events.InformationEvent;
import de.stem.stemSystem.modules.notificationModule.NotificationPriority;
import de.stem.stemSystem.modules.notificationModule.events.NotificationEvent;

public class DefaultInformationBlockListener {

    @StemEventHandler()
    public void onInformationEvent(InformationEvent informationEvent) {
        STEMSystemApp.LOGGER.INFO("New InformationEvent fired!");
        STEMSystemApp.LOGGER.INFO("Name: " + informationEvent.getInformationBlock().getName());
        STEMSystemApp.LOGGER.INFO("Source: " + informationEvent.getInformationBlock().getSourcePlugin().getPluginName());
        STEMSystemApp.LOGGER.INFO("Description: " + informationEvent.getInformationBlock().getDescription());
    }
}
