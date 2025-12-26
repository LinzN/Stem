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

public class TaskMeta {
    long taskId;
    boolean isCanceled;
    boolean runInCore;
    STEMPlugin owner;

    public TaskMeta(STEMPlugin owner, boolean runInCore) {
        this.owner = owner;
        this.runInCore = runInCore;
        this.taskId = System.nanoTime();
        this.isCanceled = false;
    }

    public void cancel() {
        if (!this.isCanceled) {
            STEMApp.LOGGER.DEBUG("Cancel task " + taskId + " from plugin " + owner.getPluginName());
        }
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

    public STEMPlugin getOwner() {
        return owner;
    }
}
