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

package de.linzn.stem.modules.commandModule.defaultCommands;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.commandModule.ICommand;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UptimeCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        Date date = STEMApp.getInstance().getUptimeDate();

        long diff = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - date.getTime());

        String uptime = String.format("%d days, %02d:%02d:%02d", (diff / (3600 * 24)), diff / 3600, (diff % 3600) / 60, (diff % 60));
        STEMApp.LOGGER.LIVE("Uptime: " + uptime);
        return true;
    }

}
