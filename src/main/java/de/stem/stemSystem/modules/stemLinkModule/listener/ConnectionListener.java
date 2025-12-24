/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.stem.stemSystem.modules.stemLinkModule.listener;

import de.linzn.stemLink.components.events.ConnectEvent;
import de.linzn.stemLink.components.events.DisconnectEvent;
import de.linzn.stemLink.components.events.handler.EventHandler;
import de.stem.stemSystem.STEMSystemApp;

public class ConnectionListener {


    @EventHandler
    public void onConnectEvent(ConnectEvent event) {
        STEMSystemApp.LOGGER.SUPER("New stemLink client connected");
        STEMSystemApp.LOGGER.SUPER("UUID: " + event.getClientUUID());
        STEMSystemApp.LOGGER.SUPER("ClientType: " + event.getConnection().getClientType().name());

    }

    @EventHandler
    public void onDisconnectEvent(DisconnectEvent event) {
        STEMSystemApp.LOGGER.SUPER("StemLink client disconnected");
        STEMSystemApp.LOGGER.SUPER("UUID: " + event.getClientUUID());
        STEMSystemApp.LOGGER.SUPER("ClientType: " + event.getConnection().getClientType().name());
    }
}
