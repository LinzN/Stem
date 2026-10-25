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

package de.linzn.stem.core.api;

import de.linzn.stem.STEMApp;
import de.linzn.stem.api.notification.NotificationService;
import de.linzn.stem.modules.notificationModule.NotificationPriority;
import de.linzn.stem.modules.notificationModule.archive.ArchivedNotification;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.List;

final class NotificationServiceAdapter implements NotificationService {
    private final STEMApp stemApp;
    private final STEMPlugin owner;

    NotificationServiceAdapter(STEMApp stemApp, STEMPlugin owner) {
        this.stemApp = stemApp;
        this.owner = owner;
    }

    @Override
    public void push(String message) {
        this.push(message, NotificationPriority.DEFAULT);
    }

    @Override
    public void push(String message, NotificationPriority priority) {
        this.stemApp.getNotificationModule().pushNotification(message, priority, owner);
    }

    @Override
    public List<ArchivedNotification> getRecent() {
        return List.copyOf(this.stemApp.getNotificationModule().getNotificationArchive().getLastNotifications());
    }
}
