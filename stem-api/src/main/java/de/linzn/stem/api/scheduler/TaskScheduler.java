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

import de.linzn.stem.taskManagment.AbstractCallback;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * Schedules tasks for the owner plugin.
 * <p>
 * Async tasks run on virtual threads and may block. Core tasks run one after another on the single STEM
 * core thread and must be short. A repeating task never runs twice at the same time: if a run is still
 * active when the next one is due, the next one is skipped. Exceptions thrown by tasks are logged.
 * Times of day and cron patterns use the Europe/Berlin time zone.
 */
public interface TaskScheduler {

    /**
     * Run a task asynchronously as soon as possible.
     */
    ScheduledTask runAsync(Runnable task);

    /**
     * Run a task on the core thread as soon as possible.
     */
    ScheduledTask runInCore(Runnable task);

    /**
     * Run a task asynchronously after a delay.
     */
    ScheduledTask runLater(Runnable task, long delay, TimeUnit unit);

    /**
     * Run a task on the core thread after a delay.
     */
    ScheduledTask runLaterInCore(Runnable task, long delay, TimeUnit unit);

    /**
     * Run a task asynchronously at a fixed rate.
     *
     * @param initialDelay delay before the first run
     * @param period       time between runs, must be greater than zero
     */
    ScheduledTask runRepeating(Runnable task, long initialDelay, long period, TimeUnit unit);

    /**
     * Run a task on the core thread at a fixed rate.
     */
    ScheduledTask runRepeatingInCore(Runnable task, long initialDelay, long period, TimeUnit unit);

    /**
     * Run a task asynchronously every day at the given local time (Europe/Berlin).
     */
    ScheduledTask runDaily(Runnable task, LocalTime time);

    /**
     * Run a task asynchronously once at the next occurrence of the given local time (Europe/Berlin).
     */
    ScheduledTask runOnceAt(Runnable task, LocalTime time);

    /**
     * Run a task asynchronously according to a cron pattern (cron4j syntax, Europe/Berlin).
     * An invalid pattern is logged and a cancelled task is returned.
     */
    ScheduledTask runCron(Runnable task, String cronPattern);

    /**
     * Register a callback that is scheduled according to its {@link AbstractCallback#getTime()}.
     */
    void registerCallback(AbstractCallback callback);

    /**
     * Unregister a callback and cancel its schedule.
     */
    void unregisterCallback(AbstractCallback callback);

    /**
     * Cancel all tasks and callbacks of the owner plugin.
     */
    void cancelAll();
}
