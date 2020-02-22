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


import de.azcore.azcoreRuntime.AppLogger;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoreRunner implements Runnable {

    private AtomicBoolean isAlive = new AtomicBoolean();
    private SchedulerService schedulerService;
    private CallbackService callbackService;
    private LinkedList<Runnable> taskQueue;

    public CoreRunner() {
        this.schedulerService = new SchedulerService(this);
        this.callbackService = new CallbackService();
        this.taskQueue = new LinkedList<>();
        isAlive.set(true);
    }

    public void run() {
        while (isAlive.get()) {
            System.out.print("|");
            if (!this.taskQueue.isEmpty()) {
                Runnable task = this.taskQueue.remove();
                AppLogger.logger("Exec Task: " + task.getClass().getName(), false, false);
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }

    void queueTask(Runnable runnable) {
        this.taskQueue.addLast(runnable);
    }


    public SchedulerService getSchedulerService() {
        return this.schedulerService;
    }

    public void endCore() {
        this.schedulerService.cancelAll();
        this.isAlive.set(false);
    }

    public CallbackService getCallbackService() {
        return this.callbackService;
    }
}
