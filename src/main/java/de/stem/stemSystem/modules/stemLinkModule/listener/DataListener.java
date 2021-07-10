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


import de.linzn.stemLink.components.events.ReceiveDataEvent;
import de.linzn.stemLink.components.events.handler.EventHandler;
import de.stem.stemSystem.STEMSystemApp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class DataListener {

    @EventHandler(channel = "default_stream")
    public void onReceiveEvent(ReceiveDataEvent event) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getDataInBytes()));
        String values = null;
        try {
            values = in.readUTF();
        } catch (IOException e) {
            STEMSystemApp.LOGGER.ERROR(e);
        }

    }


}
