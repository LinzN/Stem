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

package de.azcore.azcoreRuntime.module.network.mask;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.linzn.zSocket.components.IZMask;

public class SocketMask implements IZMask {
    @Override
    public void runThread(Runnable runnable) {
        AZCoreRuntimeApp.getInstance().getHeartbeat().runTask(runnable);
    }

    @Override
    public boolean isDebugging() {
        return true;
    }
}
