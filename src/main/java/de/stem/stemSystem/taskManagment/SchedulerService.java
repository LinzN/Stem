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
    private final DefaultSTEMPlugin defaultSystemPlugin;
    private HashSet<TaskMeta> tasks;

    SchedulerService(CoreRunner coreRunner) {
        this.coreRunner = coreRunner;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(50);
        this.executorService = new ThreadPoolExecutor(30, 30, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.tasks = new HashSet<>();
        this.defaultSystemPlugin = new DefaultSTEMPlugin();
    }

    public TaskMeta runTaskInCore(STEMPlugin plugin, Runnable task) {
        return execTask(plugin, task, true);
    }

    public TaskMeta runTask(STEMPlugin plugin, Runnable task) {
        return execTask(plugin, task, false);
    }


    public TaskMeta runTaskLaterInCore(STEMPlugin plugin, Runnable task, long delay, TimeUnit timeUnit) {
        return this.execTaskDelay(plugin, task, delay, timeUnit, true);
    }

    public TaskMeta runTaskLater(STEMPlugin plugin, Runnable task, long delay, TimeUnit timeUnit) {
        return this.execTaskDelay(plugin, task, delay, timeUnit, false);
    }


    public TaskMeta runRepeatSchedulerInCore(STEMPlugin plugin, Runnable task, int delay, int period, TimeUnit timeUnit) {
        return this.execRepeatTask(plugin, task, delay, period, timeUnit, true);
    }

    public TaskMeta runRepeatScheduler(STEMPlugin plugin, Runnable task, int delay, int period, TimeUnit timeUnit) {
        return this.execRepeatTask(plugin, task, delay, period, timeUnit, false);
    }

    public TaskMeta runFixedSchedulerInCore(STEMPlugin plugin, Runnable task, int days, int hours, int minutes, boolean daily) {
        return this.execFixedTask(plugin, task, days, hours, minutes, daily, true);
    }

    public TaskMeta runFixedScheduler(STEMPlugin plugin, Runnable task, int days, int hours, int minutes, boolean daily) {
        return this.execFixedTask(plugin, task, days, hours, minutes, daily, false);
    }


    private TaskMeta execFixedTask(STEMPlugin plugin, Runnable task, int days, int hours, int minutes, boolean daily, boolean runInCore) {
        if (!this.checkIsValid()) {
            return null;
        }

        TaskMeta taskMeta = new TaskMeta(plugin, runInCore);
        this.tasks.add(taskMeta);
        STEMSystemApp.LOGGER.DEBUG("TaskMeta setup fixed task from owner:" + plugin.getPluginName() + " taskId:" + taskMeta.taskId);
        long times = this.getTimerTime(days, hours, minutes);
        Runnable runnableContainer = () -> {
            while (!taskMeta.isCanceled) {
                this.pushCoreRunner(taskMeta, task);

                if (daily) {
                    try {
                        Thread.sleep(TimeUnit.HOURS.toMillis(24));
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    taskMeta.cancel();
                }
            }
            tasks.remove(taskMeta);
        };

        this.scheduledExecutorService.schedule(runnableContainer, times, TimeUnit.MILLISECONDS);
        return taskMeta;
    }

    private TaskMeta execRepeatTask(STEMPlugin plugin, Runnable task, int delay, int period, TimeUnit timeUnit, boolean runInCore) {
        if (!this.checkIsValid()) {
            return null;
        }

        TaskMeta taskMeta = new TaskMeta(plugin, runInCore);
        this.tasks.add(taskMeta);
        STEMSystemApp.LOGGER.DEBUG("TaskMeta setup repeat task from owner:" + plugin.getPluginName() + " taskId:" + taskMeta.taskId);
        Runnable runnableContainer = () -> {
            while (!taskMeta.isCanceled) {
                this.pushCoreRunner(taskMeta, task);

                try {
                    Thread.sleep(timeUnit.toMillis(period));
                } catch (InterruptedException ignored) {
                }
            }
            tasks.remove(taskMeta);
        };

        this.scheduledExecutorService.schedule(runnableContainer, delay, timeUnit);
        return taskMeta;
    }

    private TaskMeta execTask(STEMPlugin plugin, Runnable task, boolean runInCore) {
        if (!this.checkIsValid()) {
            return null;
        }

        TaskMeta taskMeta = new TaskMeta(plugin, runInCore);
        this.tasks.add(taskMeta);
        STEMSystemApp.LOGGER.DEBUG("TaskMeta setup task from owner:" + plugin.getPluginName() + " taskId:" + taskMeta.taskId);

        tasks.remove(taskMeta);
        if (!taskMeta.isCanceled) {
            this.pushCoreRunner(taskMeta, task);
        }

        return taskMeta;
    }

    private TaskMeta execTaskDelay(STEMPlugin plugin, Runnable task, long delay, TimeUnit timeUnit, boolean runInCore) {
        if (!this.checkIsValid()) {
            return null;
        }

        TaskMeta taskMeta = new TaskMeta(plugin, runInCore);
        this.tasks.add(taskMeta);
        STEMSystemApp.LOGGER.DEBUG("TaskMeta setup delayed task from owner: " + plugin.getPluginName() + " taskId:" + taskMeta.taskId);
        Runnable runnableContainer = () -> {
            tasks.remove(taskMeta);
            if (!taskMeta.isCanceled) {
                this.pushCoreRunner(taskMeta, task);
            }
        };

        this.scheduledExecutorService.schedule(runnableContainer, delay, timeUnit);
        return taskMeta;
    }

    private void pushCoreRunner(TaskMeta taskMeta, Runnable task) {
        if (taskMeta.runInCore) {
            this.coreRunner.queueTask(new Pair<>(taskMeta, task));
        } else {
            Runnable asyncTask = () -> executorService.submit(task);
            this.coreRunner.queueTask(new Pair<>(taskMeta, asyncTask));
        }
    }

    public boolean isTask(long taskId) {
        for (TaskMeta taskMeta : this.tasks) {
            if (taskMeta.getTaskId() == taskId) {
                return true;
            }
        }
        return false;
    }

    public void cancelTask(long taskId) {
        for (TaskMeta taskMeta : this.tasks) {
            if (taskMeta.getTaskId() == taskId) {
                taskMeta.cancel();
                break;
            }
        }
    }

    public void cancelTasks(STEMPlugin stemPlugin) {
        for (TaskMeta taskMeta : this.tasks) {
            if (taskMeta.owner == stemPlugin) {
                taskMeta.cancel();
            }
        }
    }

    void cancelAll() {
        for (TaskMeta taskMeta : this.tasks) {
            taskMeta.cancel();
        }
        this.tasks.clear();
        this.tasks = null;
    }

    public HashSet<TaskMeta> getTasks() {
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

    public DefaultSTEMPlugin getDefaultSystemPlugin() {
        return defaultSystemPlugin;
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
    }

}
