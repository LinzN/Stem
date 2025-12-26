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

package de.linzn.stem.modules.notificationModule;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.notificationModule.archive.ArchivedNotification;
import de.linzn.stem.modules.notificationModule.archive.NotificationArchive;
import de.linzn.stem.modules.notificationModule.events.NotificationEvent;
import de.linzn.stem.modules.notificationModule.listener.NotificationListener;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.Date;
import java.util.LinkedList;

public class NotificationModule extends AbstractModule {
    private final STEMApp stemApp;
    private final LinkedList<NotificationContainer> notificationQueue;
    private final NotificationArchive notificationArchive;
    private boolean moduleAlive;


    public NotificationModule(STEMApp stemApp) {
        this.stemApp = stemApp;
        this.notificationQueue = new LinkedList<>();
        this.notificationArchive = new NotificationArchive();
        this.moduleAlive = true;
        this.stemApp.getEventModule().getStemEventBus().register(new NotificationListener());
        startNotificationModule();
    }

    public void pushNotification(String message) {
        STEMPlugin stemPlugin = this.stemApp.getScheduler().getDefaultSystemPlugin();
        pushNotification(message, NotificationPriority.DEFAULT, stemPlugin);
    }

    public void pushNotification(String message, STEMPlugin stemPlugin) {
        pushNotification(message, NotificationPriority.DEFAULT, stemPlugin);
    }

    public void pushNotification(String message, NotificationPriority notificationPriority) {
        STEMPlugin stemPlugin = this.stemApp.getScheduler().getDefaultSystemPlugin();
        pushNotification(message, notificationPriority, stemPlugin);
    }

    public void pushNotification(String message, NotificationPriority notificationPriority, STEMPlugin stemPlugin) {
        NotificationContainer notificationContainer = new NotificationContainer(message, notificationPriority);
        notificationQueue.add(notificationContainer);
        this.notificationArchive.addToArchive(new ArchivedNotification(stemPlugin.getPluginName(), notificationContainer.notification, new Date()));
    }

    public NotificationArchive getNotificationArchive() {
        return notificationArchive;
    }

    private void startNotificationModule() {
        this.stemApp.getScheduler().runTask(this.getModulePlugin(), this::run);
    }

    public void stopNotificationModule() {
        this.moduleAlive = false;
    }

    @Override
    public void onShutdown() {
        this.stopNotificationModule();
    }

    private void run() {
        moduleAlive = true;
        while (moduleAlive) {
            if (!notificationQueue.isEmpty()) {
                NotificationContainer notificationContainer = notificationQueue.removeFirst();

                if (notificationContainer != null) {
                    NotificationEvent notificationEvent = new NotificationEvent(notificationContainer.notification, notificationContainer.notificationPriority);
                    STEMApp.getInstance().getEventModule().getStemEventBus().fireEvent(notificationEvent);
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
