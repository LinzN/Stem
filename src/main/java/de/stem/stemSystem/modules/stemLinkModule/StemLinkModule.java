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

package de.stem.stemSystem.modules.stemLinkModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.connections.server.StemLinkServer;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import de.stem.stemSystem.modules.stemLinkModule.listener.ConnectionListener;
import de.stem.stemSystem.modules.stemLinkModule.listener.DataListener;
import de.stem.stemSystem.modules.stemLinkModule.mask.StemLinkMask;

import java.io.File;
import java.util.Arrays;


public class StemLinkModule extends AbstractModule {
    private final StemLinkServer stemLinkServer;

    private FileConfiguration fileConfiguration;

    private String socketHost;
    private int socketPort;
    private String cryptAESKey;
    private byte[] vector16B;

    public StemLinkModule(STEMSystemApp stemSystemApp) {
        this.initConfig();
        CryptContainer cryptContainer = new CryptContainer(this.cryptAESKey, this.vector16B);
        this.stemLinkServer = new StemLinkServer(this.socketHost, this.socketPort, new StemLinkMask(this), cryptContainer);
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
        this.stemLinkServer.registerEvents(new ConnectionListener());
        this.stemLinkServer.registerEvents(new DataListener());
    }

    public void createNetwork() {
        this.stemLinkServer.openServer();
    }

    public void deleteNetwork() {
        this.stemLinkServer.closeServer();
    }

    public StemLinkServer getStemLinkServer() {
        return this.stemLinkServer;
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_stemLink.yml"));

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
