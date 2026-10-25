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

package de.linzn.stem.modules.notificationModule;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.notificationModule.archive.ArchivedNotification;
import de.linzn.stem.modules.notificationModule.archive.NotificationArchive;
import de.linzn.stem.modules.notificationModule.events.NotificationEvent;
import de.linzn.stem.modules.notificationModule.listener.NotificationListener;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class NotificationModule extends AbstractModule {
    private static final int MAX_QUEUE_SIZE = 10_000;
    private final LinkedBlockingQueue<NotificationContainer> notificationQueue;
    private final NotificationArchive notificationArchive;
    private volatile boolean moduleAlive;
    private volatile Thread workerThread;

    public NotificationModule(STEMApp stemApp) {
        super(stemApp);
        this.notificationQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        this.notificationArchive = new NotificationArchive();
        this.moduleAlive = true;
        this.getStemApp().getEventModule().getStemEventBus().register(new NotificationListener(), this.getModulePlugin());
        startNotificationModule();
    }

    public void pushNotification(String message) {
        STEMPlugin stemPlugin = this.getStemApp().getScheduler().getDefaultSystemPlugin();
        pushNotification(message, NotificationPriority.DEFAULT, stemPlugin);
    }

    public void pushNotification(String message, STEMPlugin stemPlugin) {
        pushNotification(message, NotificationPriority.DEFAULT, stemPlugin);
    }

    public void pushNotification(String message, NotificationPriority notificationPriority) {
        STEMPlugin stemPlugin = this.getStemApp().getScheduler().getDefaultSystemPlugin();
        pushNotification(message, notificationPriority, stemPlugin);
    }

    public void pushNotification(String message, NotificationPriority notificationPriority, STEMPlugin stemPlugin) {
        if (message == null) {
            return;
        }
        NotificationPriority priority = notificationPriority == null ? NotificationPriority.DEFAULT : notificationPriority;
        String source = stemPlugin == null ? "UNKNOWN" : stemPlugin.getPluginName();
        NotificationContainer notificationContainer = new NotificationContainer(message, priority);
        if (!notificationQueue.offer(notificationContainer)) {
            STEMApp.LOGGER.ERROR("Notification queue is full. Dropping notification from " + source);
            return;
        }
        this.notificationArchive.addToArchive(new ArchivedNotification(source, notificationContainer.notification, new Date()));
    }

    public NotificationArchive getNotificationArchive() {
        return notificationArchive;
    }

    private void startNotificationModule() {
        this.workerThread = Thread.ofVirtual().name("STEM-Notification").start(this::run);
    }

    public void stopNotificationModule() {
        this.moduleAlive = false;
        Thread thread = this.workerThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onShutdown() {
        this.stopNotificationModule();
    }

    private void run() {
        while (moduleAlive) {
            NotificationContainer notificationContainer;
            try {
                notificationContainer = notificationQueue.take();
            } catch (InterruptedException e) {
                continue;
            }
            try {
                NotificationEvent notificationEvent = new NotificationEvent(notificationContainer.notification, notificationContainer.notificationPriority);
                STEMApp.getInstance().getEventModule().getStemEventBus().fireEvent(notificationEvent);
            } catch (Exception e) {
                STEMApp.LOGGER.ERROR(e);
            }
        }
    }
}
