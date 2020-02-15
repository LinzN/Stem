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

package de.azcore.azcoreRuntime.module.network;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.internal.containers.Module;
import de.azcore.azcoreRuntime.module.network.listener.ConnectionListener;
import de.azcore.azcoreRuntime.module.network.listener.DataListener;
import de.azcore.azcoreRuntime.module.network.mask.SocketMask;
import de.linzn.zSocket.components.encryption.CryptContainer;
import de.linzn.zSocket.connections.server.ZServer;


public class NetworkModule extends Module {
    private ZServer zServer;

    private AZCoreRuntimeApp azCoreRuntime;
    private CryptContainer cryptContainer;

    public NetworkModule(AZCoreRuntimeApp azCoreRuntime) {
        this.azCoreRuntime = azCoreRuntime;
        this.cryptContainer = new CryptContainer(this.azCoreRuntime.getAppConfigurationModule().cryptAESKey, this.azCoreRuntime.getAppConfigurationModule().vector16B);
        this.zServer = new ZServer(this.azCoreRuntime.getAppConfigurationModule().socketHost, this.azCoreRuntime.getAppConfigurationModule().socketPort, new SocketMask(), this.cryptContainer);
        this.registerEvents();
        this.createNetwork();
    }

    private void registerEvents() {
        this.zServer.registerEvents(new ConnectionListener());
        this.zServer.registerEvents(new DataListener());
    }

    public void createNetwork() {
        this.zServer.openServer();
    }

    public void deleteNetwork() {
        this.zServer.closeServer();
    }

    public ZServer getzServer() {
        return this.zServer;
    }
}
