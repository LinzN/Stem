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

package de.linzn.stem.core.api;

import de.linzn.stem.STEMApp;
import de.linzn.stem.api.scheduler.ScheduledTask;
import de.linzn.stem.api.scheduler.TaskScheduler;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import de.linzn.stem.taskManagment.AbstractCallback;
import de.linzn.stem.taskManagment.SchedulerService;

import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

final class TaskSchedulerAdapter implements TaskScheduler {
    private final STEMApp stemApp;
    private final STEMPlugin owner;

    TaskSchedulerAdapter(STEMApp stemApp, STEMPlugin owner) {
        this.stemApp = stemApp;
        this.owner = owner;
    }

    private SchedulerService scheduler() {
        return this.stemApp.getScheduler();
    }

    @Override
    public ScheduledTask runAsync(Runnable task) {
        return scheduler().runTask(owner, Objects.requireNonNull(task, "task"));
    }

    @Override
    public ScheduledTask runInCore(Runnable task) {
        return scheduler().runTaskInCore(owner, Objects.requireNonNull(task, "task"));
    }

    @Override
    public ScheduledTask runLater(Runnable task, long delay, TimeUnit unit) {
        return scheduler().runTaskLater(owner, Objects.requireNonNull(task, "task"), delay, unit);
    }

    @Override
    public ScheduledTask runLaterInCore(Runnable task, long delay, TimeUnit unit) {
        return scheduler().runTaskLaterInCore(owner, Objects.requireNonNull(task, "task"), delay, unit);
    }

    @Override
    public ScheduledTask runRepeating(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduler().runRepeating(owner, Objects.requireNonNull(task, "task"), initialDelay, period, unit, false);
    }

    @Override
    public ScheduledTask runRepeatingInCore(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduler().runRepeating(owner, Objects.requireNonNull(task, "task"), initialDelay, period, unit, true);
    }

    @Override
    public ScheduledTask runDaily(Runnable task, LocalTime time) {
        return scheduler().runFixedScheduler(owner, Objects.requireNonNull(task, "task"), 0, time.getHour(), time.getMinute(), true);
    }

    @Override
    public ScheduledTask runOnceAt(Runnable task, LocalTime time) {
        return scheduler().runFixedScheduler(owner, Objects.requireNonNull(task, "task"), 0, time.getHour(), time.getMinute(), false);
    }

    @Override
    public ScheduledTask runCron(Runnable task, String cronPattern) {
        return scheduler().runAsCronTask(owner, Objects.requireNonNull(task, "task"), cronPattern);
    }

    @Override
    public void registerCallback(AbstractCallback callback) {
        this.stemApp.getCallBackService().registerCallbackListener(callback, owner);
    }

    @Override
    public void unregisterCallback(AbstractCallback callback) {
        this.stemApp.getCallBackService().unregisterCallbackListener(callback);
    }

    @Override
    public void cancelAll() {
        this.stemApp.getCallBackService().unregisterCallbackListeners(owner);
        scheduler().cancelTasks(owner);
    }
}
