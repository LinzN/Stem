/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.azcore.azcoreRuntime.module.network.listener;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.linzn.zSocket.components.events.ConnectEvent;
import de.linzn.zSocket.components.events.DisconnectEvent;
import de.linzn.zSocket.components.events.IListener;
import de.linzn.zSocket.components.events.handler.EventHandler;

public class ConnectionListener implements IListener {


    @EventHandler
    public void onConnectEvent(ConnectEvent event) {
        AZCoreRuntimeApp.logger("Register new communication device: " + event.getClientUUID(), true, false);

    }

    @EventHandler
    public void onDisconnectEvent(DisconnectEvent event) {
        AZCoreRuntimeApp.logger("Unregister communication device: " + event.getClientUUID(), true, false);
    }
}
