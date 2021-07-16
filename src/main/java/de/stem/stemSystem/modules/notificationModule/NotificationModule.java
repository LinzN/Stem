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

package de.stem.stemSystem.modules.notificationModule;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import de.stem.stemSystem.modules.notificationModule.archive.NotificationArchive;
import de.stem.stemSystem.modules.notificationModule.archive.NotificationArchiveObject;
import de.stem.stemSystem.modules.notificationModule.events.NotificationEvent;
import de.stem.stemSystem.modules.notificationModule.listener.NotificationListener;
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;

import java.util.Date;
import java.util.LinkedList;

public class NotificationModule extends AbstractModule {
    private final STEMSystemApp stemSystemApp;
    private final LinkedList<NotificationContainer> notificationQueue;
    private final NotificationArchive notificationArchive;
    private boolean moduleAlive;


    public NotificationModule(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
        this.notificationQueue = new LinkedList<>();
        this.notificationArchive = new NotificationArchive();
        this.moduleAlive = true;
        this.stemSystemApp.getEventModule().getStemEventBus().register(new NotificationListener());
        startNotificationModule();
    }

    public void pushNotification(String message) {
        STEMPlugin stemPlugin = this.stemSystemApp.getScheduler().getDefaultSystemPlugin();
        pushNotification(message, NotificationPriority.DEFAULT, stemPlugin);
    }

    public void pushNotification(String message, STEMPlugin stemPlugin) {
        pushNotification(message, NotificationPriority.DEFAULT, stemPlugin);
    }

    public void pushNotification(String message, NotificationPriority notificationPriority) {
        STEMPlugin stemPlugin = this.stemSystemApp.getScheduler().getDefaultSystemPlugin();
        pushNotification(message, notificationPriority, stemPlugin);
    }

    public void pushNotification(String message, NotificationPriority notificationPriority, STEMPlugin stemPlugin) {
        NotificationContainer notificationContainer = new NotificationContainer(message, notificationPriority);
        notificationQueue.add(notificationContainer);
        this.notificationArchive.addToArchive(new NotificationArchiveObject(stemPlugin.getPluginName(), notificationContainer.notification, new Date()));
    }

    public NotificationArchive getNotificationArchive() {
        return notificationArchive;
    }

    private void startNotificationModule() {
        this.stemSystemApp.getScheduler().runTask(this.getModulePlugin(), this::run);
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
                    STEMSystemApp.getInstance().getEventModule().getStemEventBus().fireEvent(notificationEvent);
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
