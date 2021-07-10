/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem.modules.eventModule.listener;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.eventModule.events.StemStartupEvent;
import de.stem.stemSystem.modules.eventModule.handler.StemEventHandler;

public class StemStartupListener {

    @StemEventHandler()
    public void onStartup(StemStartupEvent event){
        STEMSystemApp.LOGGER.CORE("STEM-System startup finished in " + event.getStartupTime() + " ms.");
    }
}
