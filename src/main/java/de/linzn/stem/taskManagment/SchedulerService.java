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
import de.linzn.openJL.system.FileSystem;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import de.linzn.stem.utils.JavaUtils;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central task scheduler of STEM.
 * <ul>
 *     <li>Core tasks ("InCore") run sequentially on the kernel thread.</li>
 *     <li>All other tasks run on virtual threads, so blocking plugin code never starves the system.</li>
 *     <li>Timing is handled by a small platform-thread timer pool that only dispatches work;
 *     no thread sleeps on behalf of a repeating task.</li>
 *     <li>All cron tasks share a single cron4j scheduler; cancelling removes the cron entry immediately.</li>
 * </ul>
 */
public class SchedulerService {
    /* Time zone used for fixed time-of-day tasks and cron patterns */
    public static final ZoneId STEM_ZONE = ZoneId.of("Europe/Berlin");

    private final StemKernel stemKernel;
    private final ScheduledThreadPoolExecutor timerExecutor;
    private final ExecutorService executorService;
    private final Scheduler cronScheduler;
    private final DefaultSTEMPlugin defaultSystemPlugin;
    private final ConcurrentHashMap<Long, TaskMeta> tasks;
    private volatile boolean shutdown;

    SchedulerService(StemKernel stemKernel) {
        this.stemKernel = stemKernel;
        this.tasks = new ConcurrentHashMap<>();
        this.defaultSystemPlugin = new DefaultSTEMPlugin();

        AtomicInteger timerCounter = new AtomicInteger();
        this.timerExecutor = new ScheduledThreadPoolExecutor(2, runnable -> {
            Thread thread = new Thread(runnable, "STEM-Timer-" + timerCounter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        /* Drop cancelled futures from the queue right away instead of at their trigger time */
        this.timerExecutor.setRemoveOnCancelPolicy(true);
        this.timerExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.timerExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        this.executorService = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("STEM-Async-", 1).factory());

        this.cronScheduler = new Scheduler();
        this.cronScheduler.setDaemon(true);
        this.cronScheduler.setTimeZone(TimeZone.getTimeZone(STEM_ZONE));
        this.cronScheduler.start();
    }

    /**
     * Log any throwable thrown by a task. Errors are logged by message because the logger only renders
     * stack traces for exceptions.
     */
    static void logThrowable(Throwable throwable) {
        if (throwable instanceof Exception exception) {
            STEMApp.LOGGER.ERROR(exception);
        } else {
            STEMApp.LOGGER.ERROR("Uncaught error in task: " + throwable);
        }
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

    public TaskMeta runAsCronTask(STEMPlugin plugin, Runnable task, String cronString) {
        TaskMeta taskMeta = this.createTask(plugin, false);
        if (taskMeta.isCanceled) {
            return taskMeta;
        }
        if (cronString == null || !SchedulingPattern.validate(cronString)) {
            STEMApp.LOGGER.ERROR("Invalid cron pattern '" + cronString + "' from owner: " + plugin.getPluginName());
            taskMeta.cancel();
            return taskMeta;
        }
        STEMApp.LOGGER.CORE("TaskMeta setup cronJob task from owner:" + plugin.getPluginName() + " taskId:" + taskMeta.taskId);

        String cronId = this.cronScheduler.schedule(cronString, () -> this.dispatchRepeating(taskMeta, task));
        taskMeta.setHooks(() -> this.cronScheduler.deschedule(cronId), () -> this.tasks.remove(taskMeta.taskId));
        return taskMeta;
    }

    private TaskMeta execFixedTask(STEMPlugin plugin, Runnable task, int days, int hours, int minutes, boolean daily, boolean runInCore) {
        TaskMeta taskMeta = this.createTask(plugin, runInCore);
        if (taskMeta.isCanceled) {
            return taskMeta;
        }
        STEMApp.LOGGER.DEBUG("TaskMeta setup fixed task from owner:" + plugin.getPluginName() + " taskId:" + taskMeta.taskId);
        taskMeta.setHooks(null, () -> this.tasks.remove(taskMeta.taskId));
        ZonedDateTime firstRun = firstFixedRun(ZonedDateTime.now(STEM_ZONE), days, hours, minutes);
        this.scheduleFixedRun(taskMeta, task, firstRun, hours, minutes, daily);
        return taskMeta;
    }

    /**
     * Schedule one fixed run. Daily tasks reschedule themselves for the next calendar day at the same local
     * time, so daylight saving changes never shift the execution time.
     */
    private void scheduleFixedRun(TaskMeta taskMeta, Runnable task, ZonedDateTime runAt, int hours, int minutes, boolean daily) {
        if (taskMeta.isCanceled || this.shutdown) {
            return;
        }
        long delayMillis = Math.max(0, Duration.between(ZonedDateTime.now(STEM_ZONE), runAt).toMillis());
        ScheduledFuture<?> future = this.timerExecutor.schedule(() -> {
            if (taskMeta.isCanceled) {
                return;
            }
            if (daily) {
                this.dispatchRepeating(taskMeta, task);
                ZonedDateTime next = runAt.toLocalDate().plusDays(1).atTime(hours, minutes).atZone(STEM_ZONE);
                this.scheduleFixedRun(taskMeta, task, next, hours, minutes, true);
            } else {
                this.dispatch(taskMeta, task);
                taskMeta.cancel();
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
        taskMeta.setCancelHook(() -> future.cancel(false));
    }

    static ZonedDateTime firstFixedRun(ZonedDateTime now, int days, int hours, int minutes) {
        ZonedDateTime candidate = now.toLocalDate().plusDays(days).atTime(hours, minutes).atZone(STEM_ZONE);
        if (candidate.isBefore(now)) {
            candidate = now.toLocalDate().plusDays(days + 1L).atTime(hours, minutes).atZone(STEM_ZONE);
        }
        return candidate;
    }

    private TaskMeta execRepeatTask(STEMPlugin plugin, Runnable task, int delay, int period, TimeUnit timeUnit, boolean runInCore) {
        TaskMeta taskMeta = this.createTask(plugin, runInCore);
        if (taskMeta.isCanceled) {
            return taskMeta;
        }
        if (period <= 0) {
            STEMApp.LOGGER.ERROR("Invalid repeat period " + period + " from owner: " + plugin.getPluginName());
            taskMeta.cancel();
            return taskMeta;
        }
        STEMApp.LOGGER.DEBUG("TaskMeta setup repeat task from owner:" + plugin.getPluginName() + " taskId:" + taskMeta.taskId);
        ScheduledFuture<?> future = this.timerExecutor.scheduleAtFixedRate(() -> this.dispatchRepeating(taskMeta, task),
                Math.max(0, delay), period, timeUnit);
        taskMeta.setHooks(() -> future.cancel(false), () -> this.tasks.remove(taskMeta.taskId));
        return taskMeta;
    }

    private TaskMeta execTask(STEMPlugin plugin, Runnable task, boolean runInCore) {
        TaskMeta taskMeta = this.createTask(plugin, runInCore);
        if (taskMeta.isCanceled) {
            return taskMeta;
        }
        STEMApp.LOGGER.DEBUG("TaskMeta setup task from owner:" + plugin.getPluginName() + " taskId:" + taskMeta.taskId);
        /* One-shot tasks are not tracked: they are dispatched immediately */
        this.tasks.remove(taskMeta.taskId);
        this.dispatch(taskMeta, task);
        return taskMeta;
    }

    private TaskMeta execTaskDelay(STEMPlugin plugin, Runnable task, long delay, TimeUnit timeUnit, boolean runInCore) {
        TaskMeta taskMeta = this.createTask(plugin, runInCore);
        if (taskMeta.isCanceled) {
            return taskMeta;
        }
        STEMApp.LOGGER.DEBUG("TaskMeta setup delayed task from owner: " + plugin.getPluginName() + " taskId:" + taskMeta.taskId);
        ScheduledFuture<?> future = this.timerExecutor.schedule(() -> {
            this.tasks.remove(taskMeta.taskId);
            if (!taskMeta.isCanceled) {
                this.dispatch(taskMeta, task);
            }
        }, Math.max(0, delay), timeUnit);
        taskMeta.setHooks(() -> future.cancel(false), () -> this.tasks.remove(taskMeta.taskId));
        return taskMeta;
    }

    /**
     * Create and register a task. During shutdown a cancelled task is returned instead of null,
     * so callers never run into a NullPointerException.
     */
    private TaskMeta createTask(STEMPlugin plugin, boolean runInCore) {
        if (plugin == null) {
            plugin = this.defaultSystemPlugin;
        }
        TaskMeta taskMeta = new TaskMeta(plugin, runInCore);
        if (this.shutdown) {
            STEMApp.LOGGER.ERROR("Try to register task while shutdown! Owner: " + plugin.getPluginName());
            taskMeta.isCanceled = true;
            return taskMeta;
        }
        this.tasks.put(taskMeta.taskId, taskMeta);
        return taskMeta;
    }

    /**
     * Dispatch a repeating task, skipping the run if the previous run of the same task is still active.
     * This prevents unbounded pile-up when a task is slower than its period.
     */
    private void dispatchRepeating(TaskMeta taskMeta, Runnable task) {
        if (taskMeta.isCanceled || this.shutdown) {
            return;
        }
        if (!taskMeta.running.compareAndSet(false, true)) {
            STEMApp.LOGGER.DEBUG("Skip run of task " + taskMeta.taskId + " from " + taskMeta.owner.getPluginName() + ": previous run still active");
            return;
        }
        this.dispatch(taskMeta, () -> {
            try {
                task.run();
            } finally {
                taskMeta.running.set(false);
            }
        });
    }

    private void dispatch(TaskMeta taskMeta, Runnable task) {
        if (this.shutdown) {
            return;
        }
        Runnable guarded = () -> {
            try {
                task.run();
            } catch (Throwable throwable) {
                logThrowable(throwable);
            }
        };
        if (taskMeta.synchronous) {
            this.stemKernel.queueTask(new Pair<>(taskMeta, guarded));
        } else {
            try {
                this.executorService.execute(guarded);
            } catch (RejectedExecutionException e) {
                STEMApp.LOGGER.ERROR("Task " + taskMeta.taskId + " rejected: executor is shut down");
            }
        }
    }

    public boolean isTask(long taskId) {
        return this.tasks.containsKey(taskId);
    }

    public void cancelTask(long taskId) {
        TaskMeta taskMeta = this.tasks.get(taskId);
        if (taskMeta != null) {
            taskMeta.cancel();
        }
    }

    public void cancelTasks(STEMPlugin stemPlugin) {
        for (TaskMeta taskMeta : this.tasks.values()) {
            if (taskMeta.owner == stemPlugin) {
                taskMeta.cancel();
            }
        }
    }

    void cancelAll() {
        this.shutdown = true;
        for (TaskMeta taskMeta : this.tasks.values()) {
            taskMeta.cancel();
        }
        this.tasks.clear();
        this.timerExecutor.shutdownNow();
        try {
            this.cronScheduler.stop();
        } catch (IllegalStateException ignored) {
            /* already stopped */
        }
        this.executorService.shutdown();
        if (Thread.currentThread().isVirtual()) {
            /* Shutdown runs inside an async task (e.g. the stop command): do not wait for ourselves */
            return;
        }
        try {
            if (!this.executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @return snapshot of all currently scheduled (not yet finished) tasks
     */
    public HashSet<TaskMeta> getTasks() {
        return new HashSet<>(this.tasks.values());
    }

    public DefaultSTEMPlugin getDefaultSystemPlugin() {
        return defaultSystemPlugin;
    }


    public static class DefaultSTEMPlugin extends STEMPlugin {
        private volatile File tempFolder = null;

        private DefaultSTEMPlugin() {
        }

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
            try {
                return FileSystem.getFileDirectoryPath(STEMApp.class).toFile();
            } catch (URISyntaxException e) {
                STEMApp.LOGGER.ERROR(e);
            }
            return null;
        }

        @Override
        public synchronized File getTempFolder() {
            if (this.tempFolder != null) {
                return this.tempFolder;
            }
            try {
                this.tempFolder = Files.createTempDirectory(STEMApp.getInstance().getTempFolder().toPath(), this.getPluginName() + "_").toFile();
            } catch (IOException e) {
                STEMApp.LOGGER.ERROR(e);
            }
            return this.tempFolder;
        }

        @Override
        public FileConfiguration getDefaultConfig() {
            return null;
        }

        @Override
        public String getBuildJobName() {
            return "STEM";
        }

        @Override
        public String getBuildNumber() {
            return JavaUtils.getBuildNumber();
        }

        @Override
        public Path getFilePath() {
            try {
                return FileSystem.getFilePath(STEMApp.class);
            } catch (URISyntaxException e) {
                STEMApp.LOGGER.ERROR(e);
            }
            return null;
        }

    }

}
