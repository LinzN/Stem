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

package de.linzn.stem.modules.stemLinkModule.listener;


import de.linzn.stemLink.components.events.ReceiveDataEvent;
import de.linzn.stemLink.components.events.handler.EventHandler;
import de.linzn.stem.STEMApp;

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
            STEMApp.LOGGER.ERROR(e);
        }

    }


}
