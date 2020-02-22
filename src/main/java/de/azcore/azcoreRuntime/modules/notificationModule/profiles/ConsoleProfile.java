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

package de.azcore.azcoreRuntime.modules.notificationModule.profiles;

import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.modules.notificationModule.INotificationProfile;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationContainer;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationPriority;

public class ConsoleProfile implements INotificationProfile {
    @Override
    public void push(NotificationContainer notificationContainer) {
        if (notificationContainer.notificationPriority.hasPriority(NotificationPriority.LOW)) {
            AppLogger.logger("Console -> " + notificationContainer.notification, true, false);
        }
    }
}
