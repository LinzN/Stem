/*
 * Copyright (c) 2026 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.stem.modules.informationModule.listener;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.eventModule.handler.StemEventHandler;
import de.linzn.stem.modules.informationModule.events.InformationEvent;

public class DefaultInformationBlockListener {

    @StemEventHandler()
    public void onInformationEvent(InformationEvent informationEvent) {
        STEMApp.LOGGER.DEBUG("New InformationEvent fired!");
        STEMApp.LOGGER.DEBUG("Name: " + informationEvent.getInformationBlock().getName());
        STEMApp.LOGGER.DEBUG("Source: " + informationEvent.getInformationBlock().getSourcePlugin().getPluginName());
        STEMApp.LOGGER.DEBUG("Description: " + informationEvent.getInformationBlock().getDescription());
    }
}
