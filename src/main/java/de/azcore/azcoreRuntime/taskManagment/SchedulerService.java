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
import de.azcore.azcoreRuntime.modules.pluginModule.AZPlugin;
import de.azcore.azcoreRuntime.utils.Color;
import de.linzn.simplyConfiguration.FileConfiguration;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.concurrent.*;

public class SchedulerService {
    private CoreRunner coreRunner;
    private ScheduledExecutorService scheduledExecutorService;
    private ExecutorService executorService;
    private HashSet<AZTask> tasks;
    private DefaultAZPlugin defaultAZPlugin;

    public SchedulerService(CoreRunner coreRunner) {
        this.coreRunner = coreRunner;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(50);
        this.executorService = new ThreadPoolExecutor(30, 30, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.tasks = new HashSet<>();
        this.defaultAZPlugin = new DefaultAZPlugin();
    }

    public AZTask runTask(AZPlugin plugin, Runnable task) {
        return execTask(plugin, task, true);
    }

    public AZTask runTaskAsynchronously(AZPlugin plugin, Runnable task) {
        return execTask(plugin, task, false);
    }


    public AZTask runTaskLater(AZPlugin plugin, Runnable task, long delay, TimeUnit timeUnit) {
        return execTaskDelay(plugin, task, delay, timeUnit, true);
    }

    public AZTask runTaskLaterAsynchronously(AZPlugin plugin, Runnable task, long delay, TimeUnit timeUnit) {
        return execTaskDelay(plugin, task, delay, timeUnit, false);
    }


    public AZTask runRepeatScheduler(AZPlugin plugin, Runnable task, int delay, int period, TimeUnit timeUnit) {
        return execRepeatTask(plugin, task, delay, period, timeUnit, true);
    }

    public AZTask runRepeatSchedulerAsynchronously(AZPlugin plugin, Runnable task, int delay, int period, TimeUnit timeUnit) {
        return execRepeatTask(plugin, task, delay, period, timeUnit, false);
    }

    public AZTask runFixedScheduler(AZPlugin plugin, Runnable task, int days, int hours, int minutes, boolean daily) {
        return execFixedTask(plugin, task, days, hours, minutes, daily, true);
    }

    public AZTask runFixedSchedulerAsynchronously(AZPlugin plugin, Runnable task, int days, int hours, int minutes, boolean daily) {
        return execFixedTask(plugin, task, days, hours, minutes, daily, false);
    }

    /* todo remove info logger*/
    private AZTask execFixedTask(AZPlugin plugin, Runnable task, int days, int hours, int minutes, boolean daily, boolean sync) {
        if (!checkIsValid()) {
            return null;
        }

        AZTask azTask = new AZTask(plugin, sync);
        this.tasks.add(azTask);
        AZCoreRuntimeApp.logger("Delay from " + plugin.getPluginName() + " id:" + azTask.taskId, false, false);
        long times = getTimerTime(days, hours, minutes);
        Runnable runnableContainer = () -> {
            while (!azTask.isCanceled) {
                pushCoreRunner(azTask, task);

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

    private AZTask execRepeatTask(AZPlugin plugin, Runnable task, int delay, int period, TimeUnit timeUnit, boolean sync) {
        if (!checkIsValid()) {
            return null;
        }

        AZTask azTask = new AZTask(plugin, sync);
        this.tasks.add(azTask);
        AZCoreRuntimeApp.logger("Delay from " + plugin.getPluginName() + " id:" + azTask.taskId, false, false);
        Runnable runnableContainer = () -> {
            while (!azTask.isCanceled) {
                pushCoreRunner(azTask, task);

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

    private AZTask execTask(AZPlugin plugin, Runnable task, boolean sync) {
        if (!checkIsValid()) {
            return null;
        }

        AZTask azTask = new AZTask(plugin, sync);
        this.tasks.add(azTask);
        AZCoreRuntimeApp.logger("Delay from " + plugin.getPluginName() + " id:" + azTask.taskId, false, false);
        Runnable runnableContainer = () -> {
            tasks.remove(azTask);
            if (!azTask.isCanceled) {
                pushCoreRunner(azTask, task);
            }
        };
        this.executorService.submit(runnableContainer);
        return azTask;
    }

    private AZTask execTaskDelay(AZPlugin plugin, Runnable task, long delay, TimeUnit timeUnit, boolean sync) {
        if (!checkIsValid()) {
            return null;
        }

        AZTask azTask = new AZTask(plugin, sync);
        this.tasks.add(azTask);
        AZCoreRuntimeApp.logger("Delay from " + plugin.getPluginName() + " id:" + azTask.taskId, false, false);
        Runnable runnableContainer = () -> {
            tasks.remove(azTask);
            if (!azTask.isCanceled) {
                pushCoreRunner(azTask, task);
            }
        };

        this.scheduledExecutorService.schedule(runnableContainer, delay, timeUnit);
        return azTask;
    }

    private void pushCoreRunner(AZTask azTask, Runnable task) {
        if (azTask.isSync) {
            coreRunner.queueTask(task);
        } else {
            Runnable asyncTask = () -> executorService.submit(task);
            coreRunner.queueTask(asyncTask);
        }
    }

    public void cancelTask(long taskId) {
        for (AZTask azTask : this.tasks) {
            if (azTask.getTaskId() == taskId) {
                azTask.cancel();
                break;
            }
        }
    }

    public void cancelTasks(AZPlugin azPlugin) {
        for (AZTask azTask : this.tasks) {
            if (azTask.owner == azPlugin) {
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
            AZCoreRuntimeApp.logger(Color.RED + "Try to register task while shutdown!" + Color.RESET, false, false);
        }
        return this.tasks != null;
    }

    public DefaultAZPlugin getDefaultAZPlugin() {
        return defaultAZPlugin;
    }


    public class AZTask {
        private long taskId;
        private boolean isCanceled;
        private boolean isSync;
        private AZPlugin owner;

        public AZTask(AZPlugin owner, boolean isSync) {
            this.owner = owner;
            this.isSync = isSync;
            this.taskId = System.nanoTime();
        }

        public void cancel() {
            this.isCanceled = true;
        }

        public long getTaskId() {
            return taskId;
        }

        public boolean isCanceled() {
            return isCanceled;
        }

        public boolean isSync() {
            return isSync;
        }

        public AZPlugin getOwner() {
            return owner;
        }
    }

    private class DefaultAZPlugin extends AZPlugin {

        @Override
        public void onEnable() {
        }

        @Override
        public void onDisable() {
        }

        @Override
        public String getPluginName() {
            return "AZCorePlugin";
        }

        @Override
        public String getVersion() {
            return AZCoreRuntimeApp.getInstance().getVersion();
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
