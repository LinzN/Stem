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

package de.azcore.azcoreRuntime.modules.notificationModule;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.modules.Module;
import de.azcore.azcoreRuntime.modules.notificationModule.profiles.ConsoleProfile;
import de.azcore.azcoreRuntime.modules.notificationModule.profiles.SocketProfile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NotificationModule extends Module {
    private boolean alive;

    private AZCoreRuntimeApp azCoreRuntime;
    private LinkedList<NotificationContainer> notificationQueue;
    private List<INotificationProfile> notificationProfiles;

    public NotificationModule(AZCoreRuntimeApp azCoreRuntime) {
        this.azCoreRuntime = azCoreRuntime;
        this.notificationQueue = new LinkedList<>();
        this.notificationProfiles = new ArrayList<>();
        this.alive = true;
        this.registerNotificationProfile(new ConsoleProfile());
        this.registerNotificationProfile(new SocketProfile());
        startNotificationModule();
    }

    public void pushNotification(NotificationContainer notificationContainer) {
        AZCoreRuntimeApp.logger("Push Notification for profiles", false, false);
        this.azCoreRuntime.getScheduler().runTask(this.azCoreRuntime.getScheduler().getDefaultAZPlugin(), () -> notificationQueue.add(notificationContainer));
    }

    private void startNotificationModule() {
        this.azCoreRuntime.getScheduler().runTask(this.azCoreRuntime.getScheduler().getDefaultAZPlugin(), () -> {
            alive = true;
            while (alive) {
                try {
                    if (!notificationQueue.isEmpty()) {
                        NotificationContainer notificationContainer = notificationQueue.removeFirst();
                        if (notificationContainer != null) {
                            for (INotificationProfile profile : this.notificationProfiles) {
                                try {
                                    profile.push(notificationContainer);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
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
        this.alive = false;
    }

    public void registerNotificationProfile(INotificationProfile notificationProfile) {
        this.notificationProfiles.add(notificationProfile);
    }

    public void unregisterNotificationProfile(INotificationProfile notificationProfile) {
        this.notificationProfiles.remove(notificationProfile);
    }


}
