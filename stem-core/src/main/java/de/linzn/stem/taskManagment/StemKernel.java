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

import de.linzn.openJL.pairs.Pair;
import de.linzn.stem.STEMApp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The kernel executes core tasks one after another on a single thread.
 * It blocks on the queue instead of polling, so a task starts as soon as it is queued.
 */
public class StemKernel implements Runnable {

    private final AtomicBoolean isAlive = new AtomicBoolean();
    private final SchedulerService schedulerService;
    private final CallbackService callbackService;
    private final BlockingQueue<Pair<TaskMeta, Runnable>> taskQueue;
    private volatile Thread kernelThread;

    public StemKernel() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.schedulerService = new SchedulerService(this);
        this.callbackService = new CallbackService();
        isAlive.set(true);
    }

    public void run() {
        this.kernelThread = Thread.currentThread();
        STEMApp.LOGGER.CORE("Starting kernel " + Thread.currentThread().getName());
        while (isAlive.get()) {
            Pair<TaskMeta, Runnable> metaPair;
            try {
                metaPair = this.taskQueue.take();
            } catch (InterruptedException e) {
                /* interrupted by endCore or spurious: re-check alive flag */
                continue;
            }
            TaskMeta taskMeta = metaPair.getKey();
            if (taskMeta != null) {
                STEMApp.LOGGER.DEBUG("Run task from owner: " + taskMeta.owner.getPluginName() + " Synchronous: " + taskMeta.synchronous + " taskId: " + taskMeta.taskId);
            }
            try {
                metaPair.getValue().run();
            } catch (Throwable throwable) {
                SchedulerService.logThrowable(throwable);
            }
        }
        STEMApp.LOGGER.CORE("Kernel stopped");
    }

    void queueTask(Pair<TaskMeta, Runnable> metaPair) {
        if (!this.isAlive.get()) {
            return;
        }
        this.taskQueue.add(metaPair);
    }


    public SchedulerService getSchedulerService() {
        return this.schedulerService;
    }

    public void endCore() {
        STEMApp.LOGGER.CORE("Stopping kernel...");
        this.schedulerService.cancelAll();
        this.isAlive.set(false);
        this.taskQueue.clear();
        Thread thread = this.kernelThread;
        /* Do not interrupt ourselves when shutdown runs as a core task */
        if (thread != null && thread != Thread.currentThread()) {
            thread.interrupt();
        }
    }

    public CallbackService getCallbackService() {
        return this.callbackService;
    }

}
