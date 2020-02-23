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

package de.azcore.azcoreRuntime.modules.commandModule.defaultCommands;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.modules.commandModule.ICommand;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationContainer;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationPriority;

public class PushCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        if (args.length >= 1) {
            StringBuilder message = new StringBuilder();

            for (String arg : args) {
                message.append(arg).append(" ");
            }

            NotificationContainer notificationContainer = new NotificationContainer(message.toString(), NotificationPriority.DEFAULT);
            AZCoreRuntimeApp.getInstance().getNotificationModule().pushNotification(notificationContainer);
        } else {
            AppLogger.logger("Not enough input to chat send", false);
        }
        return true;
    }

}
