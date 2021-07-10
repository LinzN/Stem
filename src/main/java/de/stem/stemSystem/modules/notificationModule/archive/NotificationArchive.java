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

    private final List<NotificationArchiveObject> archiv;
    private final int max = 10;

    public NotificationArchive() {
        this.archiv = new ArrayList<>();
    }

    public void addToArchive(NotificationArchiveObject notificationArchiveObject) {
        this.archiv.add(notificationArchiveObject);
    }

    public List<NotificationArchiveObject> getLastNotifications() {
        return this.archiv.size() <= max ? this.archiv : this.archiv.subList(this.archiv.size() - max, this.archiv.size());
    }
}
