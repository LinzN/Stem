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

package de.linzn.stem.api.scheduler;

import de.linzn.stem.modules.pluginModule.STEMPlugin;

/**
 * Handle of a scheduled task.
 */
public interface ScheduledTask {

    /**
     * @return unique id of this task
     */
    long getTaskId();

    /**
     * Cancel the task. Pending runs are removed immediately; a run that is already executing finishes.
     */
    void cancel();

    /**
     * @return true if the task was cancelled or has finished (one-shot tasks)
     */
    boolean isCanceled();

    /**
     * @return true if the task runs on the core thread
     */
    boolean isSynchronous();

    /**
     * @return plugin owning the task
     */
    STEMPlugin getOwner();
}
