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

package de.linzn.stem.modules.notificationModule.archive;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Bounded archive of the most recent notifications. Older entries are dropped,
 * so the archive never grows beyond its capacity.
 */
public class NotificationArchive {

    private static final int CAPACITY = 100;
    private final ArrayDeque<ArchivedNotification> archive;
    private final int max = 10;

    public NotificationArchive() {
        this.archive = new ArrayDeque<>(CAPACITY);
    }

    public synchronized void addToArchive(ArchivedNotification archivedNotification) {
        if (this.archive.size() >= CAPACITY) {
            this.archive.removeFirst();
        }
        this.archive.addLast(archivedNotification);
    }

    /**
     * @return copy of the last notifications, oldest first
     */
    public synchronized List<ArchivedNotification> getLastNotifications() {
        List<ArchivedNotification> all = new ArrayList<>(this.archive);
        if (all.size() <= max) {
            return all;
        }
        return new ArrayList<>(all.subList(all.size() - max, all.size()));
    }
}
