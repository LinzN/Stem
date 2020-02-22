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

package de.azcore.azcoreRuntime.modules.zSocketModule.mask;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.modules.zSocketModule.ZSocketModule;
import de.linzn.zSocket.components.IZMask;

public class SocketMask implements IZMask {

    private ZSocketModule zSocketModule;

    public SocketMask(ZSocketModule zSocketModule) {
        this.zSocketModule = zSocketModule;
    }

    @Override
    public void runThread(Runnable runnable) {
        AZCoreRuntimeApp.getInstance().getScheduler().runTask(this.zSocketModule.getModulePlugin(), runnable);
    }

    @Override
    public boolean isDebugging() {
        return true;
    }
}
