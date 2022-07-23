package de.stem.stemSystem.modules.informationModule.listener;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.eventModule.handler.StemEventHandler;
import de.stem.stemSystem.modules.informationModule.events.InformationEvent;

public class DefaultInformationBlockListener {

    @StemEventHandler()
    public void onInformationEvent(InformationEvent informationEvent) {
        STEMSystemApp.LOGGER.DEBUG("New InformationEvent fired!");
        STEMSystemApp.LOGGER.DEBUG("Name: " + informationEvent.getInformationBlock().getName());
        STEMSystemApp.LOGGER.DEBUG("Source: " + informationEvent.getInformationBlock().getSourcePlugin().getPluginName());
        STEMSystemApp.LOGGER.DEBUG("Description: " + informationEvent.getInformationBlock().getDescription());
    }
}
