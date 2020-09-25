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

package de.stem.stemSystem.modules.notificationModule.archive;

import java.util.Date;

public class NotificationArchiveObject {

    public String source;
    public String notification;
    public Date date;

    public NotificationArchiveObject(String source, String notification, Date date) {
        this.source = source;
        this.notification = notification;
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

    public String getNotification() {
        return notification;
    }
}
