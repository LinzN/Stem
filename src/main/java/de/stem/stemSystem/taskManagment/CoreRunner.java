/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem.taskManagment;


import de.linzn.openJL.pairs.Pair;
import de.stem.stemSystem.STEMSystemApp;
import org.eclipse.jetty.util.BlockingArrayQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoreRunner implements Runnable {

    private final AtomicBoolean isAlive = new AtomicBoolean();
    private final SchedulerService schedulerService;
    private final CallbackService callbackService;
    private final BlockingQueue<Pair<TaskMeta, Runnable>> taskQueue;

    public CoreRunner() {
        this.schedulerService = new SchedulerService(this);
        this.callbackService = new CallbackService();
        this.taskQueue = new BlockingArrayQueue<>();
        isAlive.set(true);
    }

    public void run() {
        while (isAlive.get()) {
            if (!this.taskQueue.isEmpty()) {
                try {
                    Pair<TaskMeta, Runnable> metaPair = this.taskQueue.take();
                    STEMSystemApp.LOGGER.DEBUG("Run task from owner: " + metaPair.getKey().owner.getPluginName() + " CoreTask: " + metaPair.getKey().runInCore + " taskId: " + metaPair.getKey().taskId);
                    try {
                        metaPair.getValue().run();
                    } catch (Exception e) {
                        STEMSystemApp.LOGGER.ERROR(e);
                    }

                } catch (InterruptedException e) {
                    STEMSystemApp.LOGGER.ERROR(e);
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }

    void queueTask(Pair<TaskMeta, Runnable> metaPair) {
        this.taskQueue.add(metaPair);
    }


    public SchedulerService getSchedulerService() {
        return this.schedulerService;
    }

    public void endCore() {
        STEMSystemApp.LOGGER.CORE("Stopping CoreRunner...");
        this.schedulerService.cancelAll();
        this.isAlive.set(false);
    }

    public CallbackService getCallbackService() {
        return this.callbackService;
    }

}
