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

package de.stem.stemSystem.modules.stemLinkModule.listener;

import de.linzn.stemLink.components.events.ConnectEvent;
import de.linzn.stemLink.components.events.DisconnectEvent;
import de.linzn.stemLink.components.events.handler.EventHandler;
import de.stem.stemSystem.STEMSystemApp;

public class ConnectionListener {


    @EventHandler
    public void onConnectEvent(ConnectEvent event) {
        STEMSystemApp.LOGGER.WARNING("Register new communication device: " + event.getClientUUID());

    }

    @EventHandler
    public void onDisconnectEvent(DisconnectEvent event) {
        STEMSystemApp.LOGGER.WARNING("Unregister communication device: " + event.getClientUUID());
    }
}
