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

package de.linzn.stem.modules.notificationModule.archive;

import java.util.Date;

public class ArchivedNotification {

    public String source;
    public String notification;
    public Date date;

    public ArchivedNotification(String source, String notification, Date date) {
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
