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


import de.azcore.azcoreRuntime.AZCoreRuntimeApp;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoreRunner implements Runnable {
    private AZCoreRuntimeApp azCoreRuntime;
    private AtomicBoolean isAlive = new AtomicBoolean();
    private SchedulerService schedulerService;
    private LinkedList<Runnable> taskQueue;
    /* The list with the pending tasks*/


    public CoreRunner(AZCoreRuntimeApp azCoreRuntime) {
        this.azCoreRuntime = azCoreRuntime;
        this.schedulerService = new SchedulerService(this);
        this.taskQueue = new LinkedList<>();
        isAlive.set(true);
    }

    public void run() {
        while (isAlive.get()) {

            if (!this.taskQueue.isEmpty()) {
                Runnable task = this.taskQueue.removeFirst();
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
            }
        }
    }

    void queueTask(Runnable runnable) {
        this.taskQueue.add(runnable);
    }


    public SchedulerService getSchedulerService() {
        return this.schedulerService;
    }

    public void endCore() {
        this.schedulerService.cancelAll();
        this.isAlive.set(false);
    }
}
