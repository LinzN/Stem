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

package de.azcore.azcoreRuntime.taskManagment;

import de.azcore.azcoreRuntime.modules.pluginModule.AZPlugin;

public class AZTask {
    long taskId;
    boolean isCanceled;
    boolean runInCore;
    AZPlugin owner;

    public AZTask(AZPlugin owner, boolean runInCore) {
        this.owner = owner;
        this.runInCore = runInCore;
        this.taskId = System.nanoTime();
    }

    public void cancel() {
        this.isCanceled = true;
    }

    public long getTaskId() {
        return taskId;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isRunInCore() {
        return runInCore;
    }

    public AZPlugin getOwner() {
        return owner;
    }
}
