/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
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
import de.stem.stemSystem.modules.notificationModule.profiles.ConsoleProfile;
import de.stem.stemSystem.modules.notificationModule.profiles.SocketProfile;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class NotificationModule extends AbstractModule {
    private boolean moduleAlive;

    private final STEMSystemApp stemSystemApp;
    private final LinkedList<NotificationContainer> notificationQueue;
    private final List<INotificationProfile> notificationProfiles;
    private final NotificationArchive notificationArchive;


    public NotificationModule(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
        this.notificationQueue = new LinkedList<>();
        this.notificationProfiles = new ArrayList<>();
        this.notificationArchive = new NotificationArchive();
        this.moduleAlive = true;
        this.registerNotificationProfile(new ConsoleProfile());
        this.registerNotificationProfile(new SocketProfile());
        startNotificationModule();
    }

    public void pushNotification(NotificationContainer notificationContainer) {
        STEMSystemApp.LOGGER.WARNING("Push Notification for profiles");
        this.stemSystemApp.getScheduler().runTask(this.getModulePlugin(), () -> notificationQueue.add(notificationContainer));
        this.notificationArchive.addToArchive(new NotificationArchiveObject("SYSTEM", notificationContainer.notification, new Date()));
    }

    public NotificationArchive getNotificationArchive() {
        return notificationArchive;
    }

    private void startNotificationModule() {
        this.stemSystemApp.getScheduler().runTask(this.getModulePlugin(), () -> {
            moduleAlive = true;
            while (moduleAlive) {
                try {
                    if (!notificationQueue.isEmpty()) {
                        NotificationContainer notificationContainer = notificationQueue.removeFirst();
                        if (notificationContainer != null) {
                            for (INotificationProfile profile : this.notificationProfiles) {
                                try {
                                    profile.push(notificationContainer);
                                } catch (Exception e1) {
                                    STEMSystemApp.LOGGER.ERROR(e1);
                                }
                            }
                        }
                    }
                    Thread.sleep(50);
                } catch (Exception ignored) {
                }
            }
        });
    }

    public void stopNotificationModule() {
        this.moduleAlive = false;
    }

    public void registerNotificationProfile(INotificationProfile notificationProfile) {
        this.notificationProfiles.add(notificationProfile);
    }

    public void unregisterNotificationProfile(INotificationProfile notificationProfile) {
        this.notificationProfiles.remove(notificationProfile);
    }


    @Override
    public void onShutdown() {
        this.stopNotificationModule();
    }
}
