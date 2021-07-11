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

package de.stem.stemSystem.modules.notificationModule.listener;


import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.eventModule.handler.StemEventHandler;
import de.stem.stemSystem.modules.notificationModule.NotificationPriority;
import de.stem.stemSystem.modules.notificationModule.events.NotificationEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NotificationListener {

    @StemEventHandler()
    public void onSocketNotification(NotificationEvent notificationEvent) {
        if (notificationEvent.getNotificationPriority().hasPriority(NotificationPriority.DEFAULT)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            try {
                dataOutputStream.writeUTF(notificationEvent.getNotification());
            } catch (IOException e) {
                STEMSystemApp.LOGGER.ERROR(e);
            }
            STEMSystemApp.getInstance().getStemLinkModule().getStemLinkServer().getClients().values().forEach(serverConnection -> serverConnection.writeOutput("notification", byteArrayOutputStream.toByteArray()));
        }
    }

    @StemEventHandler()
    public void onConsoleNotification(NotificationEvent notificationEvent) {
        if (notificationEvent.getNotificationPriority().hasPriority(NotificationPriority.LOW)) {
            STEMSystemApp.LOGGER.WARNING("Console -> " + notificationEvent.getNotification());
        }
    }
}
