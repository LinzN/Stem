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


import de.linzn.simplyConfiguration.FileConfiguration;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;
import de.stem.stemSystem.utils.JavaUtils;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.concurrent.*;

public class SchedulerService {
    private final CoreRunner coreRunner;
    private final ScheduledExecutorService scheduledExecutorService;
    private final ExecutorService executorService;
    private HashSet<AZTask> tasks;
    private final DefaultSTEMPlugin defaultAZPlugin;

    SchedulerService(CoreRunner coreRunner) {
        this.coreRunner = coreRunner;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(50);
        this.executorService = new ThreadPoolExecutor(30, 30, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.tasks = new HashSet<>();
        this.defaultAZPlugin = new DefaultSTEMPlugin();
    }

    public AZTask runTaskInCore(STEMPlugin plugin, Runnable task) {
        return execTask(plugin, task, true);
    }

    public AZTask runTask(STEMPlugin plugin, Runnable task) {
        return execTask(plugin, task, false);
    }


    public AZTask runTaskLaterInCore(STEMPlugin plugin, Runnable task, long delay, TimeUnit timeUnit) {
        return this.execTaskDelay(plugin, task, delay, timeUnit, true);
    }

    public AZTask runTaskLater(STEMPlugin plugin, Runnable task, long delay, TimeUnit timeUnit) {
        return this.execTaskDelay(plugin, task, delay, timeUnit, false);
    }


    public AZTask runRepeatSchedulerInCore(STEMPlugin plugin, Runnable task, int delay, int period, TimeUnit timeUnit) {
        return this.execRepeatTask(plugin, task, delay, period, timeUnit, true);
    }

    public AZTask runRepeatScheduler(STEMPlugin plugin, Runnable task, int delay, int period, TimeUnit timeUnit) {
        return this.execRepeatTask(plugin, task, delay, period, timeUnit, false);
    }

    public AZTask runFixedSchedulerInCore(STEMPlugin plugin, Runnable task, int days, int hours, int minutes, boolean daily) {
        return this.execFixedTask(plugin, task, days, hours, minutes, daily, true);
    }

    public AZTask runFixedScheduler(STEMPlugin plugin, Runnable task, int days, int hours, int minutes, boolean daily) {
        return this.execFixedTask(plugin, task, days, hours, minutes, daily, false);
    }


    private AZTask execFixedTask(STEMPlugin plugin, Runnable task, int days, int hours, int minutes, boolean daily, boolean runInCore) {
        if (!this.checkIsValid()) {
            return null;
        }

        AZTask azTask = new AZTask(plugin, runInCore);
        this.tasks.add(azTask);
        STEMSystemApp.LOGGER.DEBUG("Fixed from " + plugin.getPluginName() + " id:" + azTask.taskId);
        long times = this.getTimerTime(days, hours, minutes);
        Runnable runnableContainer = () -> {
            while (!azTask.isCanceled) {
                this.pushCoreRunner(azTask, task);

                if (daily) {
                    try {
                        Thread.sleep(TimeUnit.HOURS.toMillis(24));
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    azTask.cancel();
                }
            }
            tasks.remove(azTask);
        };

        this.scheduledExecutorService.schedule(runnableContainer, times, TimeUnit.MILLISECONDS);
        return azTask;
    }

    private AZTask execRepeatTask(STEMPlugin plugin, Runnable task, int delay, int period, TimeUnit timeUnit, boolean runInCore) {
        if (!this.checkIsValid()) {
            return null;
        }

        AZTask azTask = new AZTask(plugin, runInCore);
        this.tasks.add(azTask);
        STEMSystemApp.LOGGER.DEBUG("Repeat from " + plugin.getPluginName() + " id:" + azTask.taskId);
        Runnable runnableContainer = () -> {
            while (!azTask.isCanceled) {
                this.pushCoreRunner(azTask, task);

                try {
                    Thread.sleep(timeUnit.toMillis(period));
                } catch (InterruptedException ignored) {
                }
            }
            tasks.remove(azTask);
        };

        this.scheduledExecutorService.schedule(runnableContainer, delay, timeUnit);
        return azTask;
    }

    private AZTask execTask(STEMPlugin plugin, Runnable task, boolean runInCore) {
        if (!this.checkIsValid()) {
            return null;
        }

        AZTask azTask = new AZTask(plugin, runInCore);
        this.tasks.add(azTask);
        STEMSystemApp.LOGGER.DEBUG("Task from " + plugin.getPluginName() + " id:" + azTask.taskId);

        tasks.remove(azTask);
        if (!azTask.isCanceled) {
            this.pushCoreRunner(azTask, task);
        }

        return azTask;
    }

    private AZTask execTaskDelay(STEMPlugin plugin, Runnable task, long delay, TimeUnit timeUnit, boolean runInCore) {
        if (!this.checkIsValid()) {
            return null;
        }

        AZTask azTask = new AZTask(plugin, runInCore);
        this.tasks.add(azTask);
        STEMSystemApp.LOGGER.DEBUG("Delay from " + plugin.getPluginName() + " id:" + azTask.taskId);
        Runnable runnableContainer = () -> {
            tasks.remove(azTask);
            if (!azTask.isCanceled) {
                this.pushCoreRunner(azTask, task);
            }
        };

        this.scheduledExecutorService.schedule(runnableContainer, delay, timeUnit);
        return azTask;
    }

    private void pushCoreRunner(AZTask azTask, Runnable task) {
        if (azTask.runInCore) {
            this.coreRunner.queueTask(task);
        } else {
            Runnable asyncTask = () -> executorService.submit(task);
            this.coreRunner.queueTask(asyncTask);
        }
    }

    public boolean isTask(long taskId) {
        for (AZTask azTask : this.tasks) {
            if (azTask.getTaskId() == taskId) {
                return true;
            }
        }
        return false;
    }

    public void cancelTask(long taskId) {
        for (AZTask azTask : this.tasks) {
            if (azTask.getTaskId() == taskId) {
                azTask.cancel();
                break;
            }
        }
    }

    public void cancelTasks(STEMPlugin stemPlugin) {
        for (AZTask azTask : this.tasks) {
            if (azTask.owner == stemPlugin) {
                azTask.cancel();
            }
        }
    }

    void cancelAll() {
        for (AZTask azTask : this.tasks) {
            azTask.cancel();
        }
        this.tasks.clear();
        this.tasks = null;
    }

    public HashSet<AZTask> getTasks() {
        return this.tasks;
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

    private boolean checkIsValid() {
        if (this.tasks == null) {
            STEMSystemApp.LOGGER.ERROR("Try to register task while shutdown!");
        }
        return this.tasks != null;
    }

    public DefaultSTEMPlugin getDefaultAZPlugin() {
        return defaultAZPlugin;
    }


    private class DefaultSTEMPlugin extends STEMPlugin {

        @Override
        public void onEnable() {
        }

        @Override
        public void onDisable() {
        }

        @Override
        public String getPluginName() {
            return "STEM";
        }

        @Override
        public String getVersion() {
            return JavaUtils.getVersion();
        }

        @Override
        public String getClassPath() {
            return null;
        }

        @Override
        public String getDescription() {
            return getPluginName() + "::" + getVersion();
        }

        @Override
        public File getDataFolder() {
            return null;
        }

        @Override
        public FileConfiguration getDefaultConfig() {
            return null;
        }

        @Override
        public void setUp(String pluginName, String version, String classPath) {

        }
    }

}
