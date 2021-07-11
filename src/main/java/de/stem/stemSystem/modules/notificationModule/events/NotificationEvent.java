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

package de.stem.stemSystem.modules.notificationModule.events;


import de.stem.stemSystem.modules.eventModule.StemEvent;
import de.stem.stemSystem.modules.notificationModule.NotificationPriority;

public class NotificationEvent implements StemEvent {

    private final String notification;
    private final NotificationPriority notificationPriority;

    public NotificationEvent(String notification, NotificationPriority notificationPriority) {
        this.notification = notification;
        this.notificationPriority = notificationPriority;
    }

    public String getNotification() {
        return notification;
    }

    public NotificationPriority getNotificationPriority() {
        return notificationPriority;
    }
}
