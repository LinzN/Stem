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

package de.stem.stemSystem.taskManagment;


import de.stem.stemSystem.AppLogger;
import org.eclipse.jetty.util.BlockingArrayQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoreRunner implements Runnable {

    private AtomicBoolean isAlive = new AtomicBoolean();
    private SchedulerService schedulerService;
    private CallbackService callbackService;
    private BlockingQueue<Runnable> taskQueue;

    public CoreRunner() {
        this.schedulerService = new SchedulerService(this);
        this.callbackService = new CallbackService();
        this.taskQueue = new BlockingArrayQueue<>();
        isAlive.set(true);
    }

    public void run() {
        while (isAlive.get()) {
            if (AppLogger.getVerbose()) {
                System.out.print("|");
            }

            if (!this.taskQueue.isEmpty()) {
                try {
                    Runnable task = this.taskQueue.take();
                    AppLogger.debug("Exec Task: " + task.getClass().getSimpleName());
                    try {
                        task.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (InterruptedException e) {
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
        this.taskQueue.add(runnable);
    }


    public SchedulerService getSchedulerService() {
        return this.schedulerService;
    }

    public void endCore() {
        AppLogger.logger("Stopping CoreRunner...", true);
        this.schedulerService.cancelAll();
        this.isAlive.set(false);
    }

    public CallbackService getCallbackService() {
        return this.callbackService;
    }

}
