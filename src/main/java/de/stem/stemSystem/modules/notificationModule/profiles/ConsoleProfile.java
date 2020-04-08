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

package de.stem.stemSystem.modules.notificationModule.profiles;

import de.stem.stemSystem.AppLogger;
import de.stem.stemSystem.modules.notificationModule.INotificationProfile;
import de.stem.stemSystem.modules.notificationModule.NotificationContainer;
import de.stem.stemSystem.modules.notificationModule.NotificationPriority;

public class ConsoleProfile implements INotificationProfile {
    @Override
    public void push(NotificationContainer notificationContainer) {
        if (notificationContainer.notificationPriority.hasPriority(NotificationPriority.LOW)) {
            AppLogger.logger("Console -> " + notificationContainer.notification, true);
        }
    }
}
