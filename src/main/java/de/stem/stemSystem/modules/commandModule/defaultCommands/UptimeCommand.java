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

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UptimeCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        Date date = STEMSystemApp.getInstance().getUptimeDate();

        long diff = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - date.getTime());

        String uptime = String.format("%d days, %02d:%02d:%02d", (diff / (3600 * 24)), diff / 3600, (diff % 3600) / 60, (diff % 60));
        STEMSystemApp.LOGGER.LIVE("Uptime: " + uptime);
        return true;
    }

}
