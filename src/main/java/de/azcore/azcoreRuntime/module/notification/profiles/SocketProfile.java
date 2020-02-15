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

package de.azcore.azcoreRuntime.module.notification.profiles;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.module.notification.INotificationProfile;
import de.azcore.azcoreRuntime.module.notification.NotificationContainer;
import de.azcore.azcoreRuntime.module.notification.NotificationPriority;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SocketProfile implements INotificationProfile {
    @Override
    public void push(NotificationContainer notificationContainer) {
        if (notificationContainer.notificationPriority.hasPriority(NotificationPriority.DEFAULT)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            try {
                dataOutputStream.writeUTF(notificationContainer.notification);
            } catch (IOException e) {
                e.printStackTrace();
            }
            AZCoreRuntimeApp.getInstance().getNetworkModule().getzServer().getClients().values().forEach(serverConnection -> serverConnection.writeOutput("notification", byteArrayOutputStream.toByteArray()));
        }
    }
}
