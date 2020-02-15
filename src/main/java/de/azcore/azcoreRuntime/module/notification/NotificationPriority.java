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

package de.azcore.azcoreRuntime.module.notification;

public enum NotificationPriority {
    LOW, DEFAULT, HIGH, ASAP;

    public boolean hasPriority(NotificationPriority notificationPriority) {
        if (this == NotificationPriority.LOW) {
            return notificationPriority == NotificationPriority.LOW;
        } else if (this == NotificationPriority.DEFAULT) {
            if (notificationPriority == NotificationPriority.LOW) {
                return true;
            } else return notificationPriority == NotificationPriority.DEFAULT;
        } else if (this == NotificationPriority.HIGH) {
            if (notificationPriority == NotificationPriority.LOW) {
                return true;
            } else if (notificationPriority == NotificationPriority.DEFAULT) {
                return true;
            } else return notificationPriority == NotificationPriority.HIGH;
        } else return this == NotificationPriority.ASAP;
    }
}
