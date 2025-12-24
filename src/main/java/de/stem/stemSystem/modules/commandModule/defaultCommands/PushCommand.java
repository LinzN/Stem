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

package de.stem.stemSystem.modules.commandModule.defaultCommands;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;
import de.stem.stemSystem.modules.notificationModule.NotificationPriority;

public class PushCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        if (args.length >= 1) {
            StringBuilder message = new StringBuilder();

            for (String arg : args) {
                message.append(arg).append(" ");
            }

            STEMSystemApp.getInstance().getNotificationModule().pushNotification(message.toString(), NotificationPriority.DEFAULT);
        } else {
            STEMSystemApp.LOGGER.LIVE("Not enough input to chat send");
        }
        return true;
    }

}
