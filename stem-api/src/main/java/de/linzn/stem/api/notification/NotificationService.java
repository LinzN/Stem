/*
 * Copyright (c) 2026 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.stem.api.notification;

import de.linzn.stem.modules.notificationModule.NotificationPriority;
import de.linzn.stem.modules.notificationModule.archive.ArchivedNotification;

import java.util.List;

/**
 * Notifications are delivered to connected StemLink clients and the console.
 */
public interface NotificationService {

    /**
     * Push a notification with default priority.
     */
    void push(String message);

    /**
     * Push a notification with the given priority.
     */
    void push(String message, NotificationPriority priority);

    /**
     * @return the most recent notifications, oldest first
     */
    List<ArchivedNotification> getRecent();
}
