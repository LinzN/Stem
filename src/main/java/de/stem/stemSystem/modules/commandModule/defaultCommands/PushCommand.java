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

package de.stem.stemSystem.modules.commandModule.defaultCommands;

import de.stem.stemSystem.AppLogger;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;
import de.stem.stemSystem.modules.notificationModule.NotificationContainer;
import de.stem.stemSystem.modules.notificationModule.NotificationPriority;

public class PushCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        if (args.length >= 1) {
            StringBuilder message = new StringBuilder();

            for (String arg : args) {
                message.append(arg).append(" ");
            }

            NotificationContainer notificationContainer = new NotificationContainer(message.toString(), NotificationPriority.DEFAULT);
            STEMSystemApp.getInstance().getNotificationModule().pushNotification(notificationContainer);
        } else {
            AppLogger.logger("Not enough input to chat send", false);
        }
        return true;
    }

}
