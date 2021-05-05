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

package de.stem.stemSystem.modules.zSocketModule.mask;

import de.linzn.zSocket.components.IZMask;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.zSocketModule.ZSocketModule;

import java.util.logging.Level;

public class SocketMask implements IZMask {

    private final ZSocketModule zSocketModule;

    public SocketMask(ZSocketModule zSocketModule) {
        this.zSocketModule = zSocketModule;
    }

    @Override
    public void runThread(Runnable runnable) {
        STEMSystemApp.getInstance().getScheduler().runTask(this.zSocketModule.getModulePlugin(), runnable);
    }

    @Override
    public boolean isDebugging() {
        return STEMSystemApp.logSystem.getLogLevel() == Level.ALL;
    }

    @Override
    public void log(String s) {
        STEMSystemApp.LOGGER.INFO(s);
    }
}
