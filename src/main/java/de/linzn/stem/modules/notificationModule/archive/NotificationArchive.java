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

import java.util.ArrayList;
import java.util.List;

public class NotificationArchive {

    private final List<ArchivedNotification> archive;
    private final int max = 10;

    public NotificationArchive() {
        this.archive = new ArrayList<>();
    }

    public void addToArchive(ArchivedNotification archivedNotification) {
        this.archive.add(archivedNotification);
    }

    public List<ArchivedNotification> getLastNotifications() {
        return this.archive.size() <= max ? this.archive : this.archive.subList(this.archive.size() - max, this.archive.size());
    }
}
