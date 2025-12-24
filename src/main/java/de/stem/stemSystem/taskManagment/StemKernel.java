/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.stem.stemSystem.taskManagment;


import de.linzn.openJL.pairs.Pair;
import de.stem.stemSystem.STEMSystemApp;
import org.eclipse.jetty.util.BlockingArrayQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class StemKernel implements Runnable {

    private final AtomicBoolean isAlive = new AtomicBoolean();
    private final SchedulerService schedulerService;
    private final CallbackService callbackService;
    private final BlockingQueue<Pair<TaskMeta, Runnable>> taskQueue;

    public StemKernel() {
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
        STEMSystemApp.LOGGER.CORE("Stopping StemKernel...");
        this.schedulerService.cancelAll();
        this.isAlive.set(false);
    }

    public CallbackService getCallbackService() {
        return this.callbackService;
    }

}
