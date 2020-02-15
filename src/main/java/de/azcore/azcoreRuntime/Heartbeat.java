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

package de.azcore.azcoreRuntime;


import de.azcore.azcoreRuntime.internal.containers.TaskOperation;
import de.azcore.azcoreRuntime.internal.containers.TimeData;
import de.azcore.azcoreRuntime.internal.containers.TimedTimeData;
import de.azcore.azcoreRuntime.module.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Heartbeat implements Runnable {
    private AZCoreRuntimeApp azCoreRuntime;
    private AtomicBoolean isAlive = new AtomicBoolean();
    /* The list with the pending tasks*/
    private LinkedList<HeartContainer> taskList;
    private LinkedHashMap<Runnable, Plugin> schedulerMap;
    private ScheduledExecutorService scheduledExecutorService;
    private ExecutorService executorService;
    private Plugin azCorePlugin;

    Heartbeat(AZCoreRuntimeApp azCoreRuntime) {
        this.executorService = new ThreadPoolExecutor(30, 30, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(50);
        this.taskList = new LinkedList<>();
        this.schedulerMap = new LinkedHashMap<>();
        this.azCorePlugin = getAzCorePlugin();
        this.azCoreRuntime = azCoreRuntime;
        isAlive.set(true);
    }

    /* The task worker */
    public void run() {
        /* Heartbeat is running until isAlive is false */
        while (isAlive.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!this.taskList.isEmpty()) {
                Runnable runnable = this.taskList.removeFirst().runnable;
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        /* Shutdown the heartbeat Thread */
        this.azCoreRuntime.getNetworkModule().deleteNetwork();
        System.exit(0);

    }

    public void runTaskSingleOperation(TaskOperation taskOperation) {
        Runnable runnable = () -> taskOperation.runOperation(null);
        this.runTask(runnable);
    }

    /* Run Task directly in the heartbeat Thread */
    public void runSyncTask(Runnable sync) {
        this.runSyncTask(sync, this.azCorePlugin);
    }

    public void runSyncTask(Runnable sync, Plugin plugin) {
        this.taskList.add(new HeartContainer(sync, plugin));
    }


    /* Run single Task in a new Thread */
    public void runTask(Runnable async) {
        this.runTask(async, null);
    }

    public void runTask(Runnable async, Plugin plugin) {
        AZCoreRuntimeApp.logger("Run task#" + async.getClass().getSimpleName(), false, true);
        Runnable runnable = () -> executorService.submit(async);
        this.taskList.add(new HeartContainer(runnable, plugin));
    }


    /* Run repeat Task in a new Thread */
    public void runDelayedScheduler(Runnable run, int delay, TimeUnit timeUnit) {
        this.runDelayedScheduler(run, this.azCorePlugin, delay, timeUnit);
    }

    public void runDelayedScheduler(Runnable run, Plugin plugin, int delay, TimeUnit timeUnit) {
        AZCoreRuntimeApp.logger("Register delayed scheduler [delay: " + delay + " unit: " + timeUnit.name() + "]#" + run.getClass().getSimpleName(), false, true);
        Runnable runnable = () -> this.scheduledExecutorService.schedule(run, delay, timeUnit);
        this.taskList.add(new HeartContainer(runnable, plugin));
    }


    /* Run repeat Task in a new Thread */
    public void runRepeatScheduler(Runnable run, TimeData timeData) {
        runRepeatScheduler(run, this.azCorePlugin, timeData.delay, timeData.period, timeData.timeUnit);
    }

    public void runRepeatScheduler(Runnable run, Plugin plugin, TimeData timeData) {
        runRepeatScheduler(run, plugin, timeData.delay, timeData.period, timeData.timeUnit);
    }

    /* Run repeat Task in a new Thread */
    public void runRepeatScheduler(Runnable run, int delay, int period, TimeUnit timeUnit) {
        runRepeatScheduler(run, this.azCorePlugin, delay, period, timeUnit);
    }

    public void runRepeatScheduler(Runnable run, Plugin plugin, int delay, int period, TimeUnit timeUnit) {
        Runnable runnable = () -> this.scheduledExecutorService.scheduleAtFixedRate(run, delay, period, timeUnit);
        HeartContainer taskContainer = new HeartContainer(runnable, plugin);
        this.taskList.add(taskContainer);
        this.schedulerMap.put(taskContainer.runnable, taskContainer.plugin);
        AZCoreRuntimeApp.logger("New repeat scheduler [delay: " + delay + " period:" + period + " unit: " + timeUnit.name() + "]#" + run.getClass().getSimpleName(), false, false);
    }

    /* Run timed Task in a new Thread */
    public void runFixedScheduler(Runnable run, TimedTimeData timedTimeData, boolean daily) {
        runFixedScheduler(run, this.azCorePlugin, timedTimeData.days, timedTimeData.hours, timedTimeData.minutes, daily);
    }

    public void runFixedScheduler(Runnable run, Plugin plugin, TimedTimeData timedTimeData, boolean daily) {
        runFixedScheduler(run, plugin, timedTimeData.days, timedTimeData.hours, timedTimeData.minutes, daily);
    }

    public void runFixedScheduler(Runnable run, int days, int hours, int minutes, boolean daily) {
        runFixedScheduler(run, this.azCorePlugin, days, hours, minutes, daily);
    }

    public void runFixedScheduler(Runnable run, Plugin plugin, int days, int hours, int minutes, boolean daily) {
        long times = getTimerTime(days, hours, minutes);
        Runnable runnable;
        if (daily) {
            runnable = () -> this.scheduledExecutorService.scheduleAtFixedRate(run, times, 1000 * 60 * 60 * 24, TimeUnit.MILLISECONDS);
        } else {
            runnable = () -> this.scheduledExecutorService.schedule(run, times, TimeUnit.MILLISECONDS);
        }
        AZCoreRuntimeApp.logger("New fixed task [time:" + new SimpleDateFormat("HH:mm").format(new Date(times + System.currentTimeMillis())) + " daily: " + daily + "]#" + run.getClass().getSimpleName(), false, false);
        HeartContainer taskContainer = new HeartContainer(runnable, plugin);
        this.taskList.add(taskContainer);
        if (daily) {
            this.schedulerMap.put(taskContainer.runnable, taskContainer.plugin);
        }
    }


    private long getTimerTime(int days, int hours, int minute) {
        long selectTime = selectTime(days, hours, minute);
        if (selectTime < System.currentTimeMillis()) {
            selectTime = selectTime(days + 1, hours, minute);
        }
        return selectTime - System.currentTimeMillis();
    }

    private long selectTime(int days, int hours, int minute) {
        Calendar selectedDay = new GregorianCalendar();
        TimeZone timezone = TimeZone.getTimeZone("Europe/Berlin");
        selectedDay.setTimeZone(timezone);
        selectedDay.add(Calendar.DATE, days);
        Calendar result = new GregorianCalendar(selectedDay.get(Calendar.YEAR),
                selectedDay.get(Calendar.MONTH), selectedDay.get(Calendar.DATE), hours,
                minute);
        return result.getTime().getTime();
    }

    public boolean isAlive() {
        return this.isAlive.get();
    }

    public void setAlive(boolean value) {
        this.isAlive.set(value);
    }

    public Map<Runnable, Plugin> getRunnableList() {
        return this.schedulerMap;
    }

    private Plugin getAzCorePlugin() {
        return new Plugin() {
            @Override
            public void onEnable() {
            }

            @Override
            public void onDisable() {
            }

            @Override
            public String getVersion() {
                return azCoreRuntime.getVersion();
            }

            @Override
            public String getPluginName() {
                return "AZCore-Runtime";
            }

            @Override
            public String getDescription() {
                return "null";
            }
        };
    }

    private class HeartContainer {
        public Plugin plugin;
        public Runnable runnable;

        public HeartContainer(Runnable runnable, Plugin plugin) {
            this.plugin = plugin;
            this.runnable = runnable;
        }
    }
}
