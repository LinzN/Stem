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

package de.stem.stemSystem.modules.zSocketModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.zSocket.components.encryption.CryptContainer;
import de.linzn.zSocket.connections.server.ZServer;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import de.stem.stemSystem.modules.zSocketModule.listener.ConnectionListener;
import de.stem.stemSystem.modules.zSocketModule.listener.DataListener;
import de.stem.stemSystem.modules.zSocketModule.mask.SocketMask;

import java.io.File;
import java.util.Arrays;


public class ZSocketModule extends AbstractModule {
    private ZServer zServer;

    private FileConfiguration fileConfiguration;

    private String socketHost;
    private int socketPort;
    private String cryptAESKey;
    private byte[] vector16B;

    public ZSocketModule(STEMSystemApp stemSystemApp) {
        this.initConfig();
        CryptContainer cryptContainer = new CryptContainer(this.cryptAESKey, this.vector16B);
        this.zServer = new ZServer(this.socketHost, this.socketPort, new SocketMask(this), cryptContainer);
        this.registerEvents();
        this.createNetwork();
    }

    private static byte[] toByteArray(String string) {
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        byte[] result = new byte[strings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Byte.parseByte(strings[i]);
        }
        return result;
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

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_zSocket.yml"));

        this.socketHost = this.fileConfiguration.getString("socketHost", "0.0.0.0");
        this.socketPort = this.fileConfiguration.getInt("socketPort", 11102);
        this.cryptAESKey = this.fileConfiguration.getString("cryptAESKey", "3979244226452948404D635166546A576D5A7134743777217A25432A462D4A61");
        this.vector16B = toByteArray(this.fileConfiguration.getString("vector16B", Arrays.toString(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7})));

        this.fileConfiguration.save();
    }

    @Override
    public void onShutdown() {
        this.deleteNetwork();
    }
}
