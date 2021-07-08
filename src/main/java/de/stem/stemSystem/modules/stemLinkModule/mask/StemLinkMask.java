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

package de.stem.stemSystem.modules.stemLinkModule.mask;

import de.linzn.stemLink.components.ILinkMask;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.stemLinkModule.StemLinkModule;

import java.util.logging.Level;

public class StemLinkMask implements ILinkMask {

    private final StemLinkModule stemLinkModule;

    public StemLinkMask(StemLinkModule stemLinkModule) {
        this.stemLinkModule = stemLinkModule;
    }

    @Override
    public void runThread(Runnable runnable) {
        STEMSystemApp.getInstance().getScheduler().runTask(this.stemLinkModule.getModulePlugin(), runnable);
    }

    @Override
    public void log(String s, Level level) {
        if (level == Level.INFO) {
            STEMSystemApp.LOGGER.INFO(s);
        } else if (level == Level.FINE) {
            STEMSystemApp.LOGGER.DEBUG(s);
        } else if (level == Level.WARNING) {
            STEMSystemApp.LOGGER.WARNING(s);
        } else if (level == Level.CONFIG) {
            STEMSystemApp.LOGGER.CONFIG(s);
        } else if (level == Level.SEVERE) {
            STEMSystemApp.LOGGER.ERROR(s);
        } else {
            STEMSystemApp.LOGGER.DEBUG(s);
        }

    }


}
