/*
 * Copyright (c) 2026 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.stem.taskManagment;

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.pluginModule.STEMPlugin;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handle for a task registered in the {@link SchedulerService}.
 * <p>
 * Cancelling a task takes effect immediately: the pending trigger (timer future or cron entry)
 * is removed and the task is dropped from the scheduler registry, so no reference to the owning
 * plugin is retained after {@link #cancel()}.
 */
public class TaskMeta {
    private static final AtomicLong ID_SEQUENCE = new AtomicLong(1);

    final long taskId;
    final boolean synchronous;
    final STEMPlugin owner;
    /* Guards against overlapping executions of the same repeating task */
    final AtomicBoolean running = new AtomicBoolean(false);
    volatile boolean isCanceled;
    /* Removes the pending trigger (timer future, cron entry, ...) */
    private volatile Runnable cancelHook;
    /* Called once after cancel to drop the task from the scheduler registry */
    private volatile Runnable unregisterHook;

    public TaskMeta(STEMPlugin owner, boolean synchronous) {
        this.owner = owner;
        this.synchronous = synchronous;
        this.taskId = ID_SEQUENCE.getAndIncrement();
        this.isCanceled = false;
    }

    void setHooks(Runnable cancelHook, Runnable unregisterHook) {
        this.cancelHook = cancelHook;
        this.unregisterHook = unregisterHook;
        /* Cancel may have raced with scheduling */
        if (this.isCanceled) {
            runHooks();
        }
    }

    void setCancelHook(Runnable cancelHook) {
        this.cancelHook = cancelHook;
        if (this.isCanceled) {
            runHooks();
        }
    }

    public void cancel() {
        if (!this.isCanceled) {
            this.isCanceled = true;
            STEMApp.LOGGER.DEBUG("Cancel task " + taskId + " from plugin " + owner.getPluginName());
        }
        runHooks();
    }

    private void runHooks() {
        Runnable hook = this.cancelHook;
        this.cancelHook = null;
        if (hook != null) {
            try {
                hook.run();
            } catch (Exception e) {
                STEMApp.LOGGER.ERROR(e);
            }
        }
        Runnable unregister = this.unregisterHook;
        this.unregisterHook = null;
        if (unregister != null) {
            unregister.run();
        }
    }

    public long getTaskId() {
        return taskId;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public STEMPlugin getOwner() {
        return owner;
    }
}
