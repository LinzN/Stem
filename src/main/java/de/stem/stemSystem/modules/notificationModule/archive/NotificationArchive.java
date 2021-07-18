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

package de.stem.stemSystem.modules.notificationModule.archive;

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
