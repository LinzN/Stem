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

package de.linzn.stem.modules.stemLinkModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.stemLinkModule.listener.ConnectionListener;
import de.linzn.stem.modules.stemLinkModule.listener.DataListener;
import de.linzn.stem.modules.stemLinkModule.mask.StemLinkWrapper;
import de.linzn.stemLink.components.encryption.CryptContainer;
import de.linzn.stemLink.connections.server.StemLinkServer;

import java.io.File;
import java.util.Arrays;


public class StemLinkModule extends AbstractModule {
    private final StemLinkServer stemLinkServer;

    private FileConfiguration fileConfiguration;

    private String socketHost;
    private int socketPort;
    private String cryptAESKey;
    private byte[] vector16B;

    public StemLinkModule(STEMApp stemApp) {
        super(stemApp);
        this.initConfig();
        CryptContainer cryptContainer = new CryptContainer(this.cryptAESKey, this.vector16B);
        this.stemLinkServer = new StemLinkServer(this.socketHost, this.socketPort, new StemLinkWrapper(this), cryptContainer);
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
