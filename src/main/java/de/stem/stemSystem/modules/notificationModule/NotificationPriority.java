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

package de.stem.stemSystem.modules.notificationModule;

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
