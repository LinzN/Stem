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

import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import de.linzn.stem.taskManagment.operations.AbstractOperation;
import de.linzn.stem.taskManagment.operations.OperationOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages callback listeners. Each listener is backed by a scheduler task; unregistering cancels that task.
 */
public class CallbackService {
    private final ConcurrentHashMap<AbstractCallback, STEMPlugin> callbackListeners;

    CallbackService() {
        this.callbackListeners = new ConcurrentHashMap<>();
    }

    public void registerCallbackListener(AbstractCallback abstractCallback, STEMPlugin stemPlugin) {
        if (abstractCallback == null || stemPlugin == null) {
            STEMApp.LOGGER.ERROR("Callback register failed: callback or plugin is null");
            return;
        }
        if (this.callbackListeners.putIfAbsent(abstractCallback, stemPlugin) != null) {
            STEMApp.LOGGER.WARNING("Callback " + abstractCallback.getClass().getSimpleName() + " is already registered");
            return;
        }
        this.enableCallbackListener(abstractCallback, stemPlugin);
    }

    public void unregisterCallbackListener(AbstractCallback abstractCallback) {
        STEMApp.core().getScheduler().cancelTask(abstractCallback.taskId);
        this.callbackListeners.remove(abstractCallback);
        abstractCallback.operationData.clear();
        STEMApp.LOGGER.DEBUG("Callback unregister: " + abstractCallback.getClass().getSimpleName());
    }

    public void unregisterCallbackListeners(STEMPlugin stemPlugin) {
        for (Map.Entry<AbstractCallback, STEMPlugin> entry : this.callbackListeners.entrySet()) {
            if (entry.getValue() == stemPlugin) {
                AbstractCallback abstractCallback = entry.getKey();
                STEMApp.core().getScheduler().cancelTask(abstractCallback.taskId);
                this.callbackListeners.remove(abstractCallback);
                abstractCallback.operationData.clear();
                STEMApp.LOGGER.DEBUG("Callback unregister: " + abstractCallback.getClass().getSimpleName() + " from " + stemPlugin.getPluginName());
            }
        }
    }

    /**
     * @return snapshot of all registered callback listeners
     */
    public HashMap<AbstractCallback, STEMPlugin> getCallbackListeners() {
        return new HashMap<>(this.callbackListeners);
    }

    private void enableCallbackListener(AbstractCallback abstractCallback, STEMPlugin plugin) {
        CallbackTime callbackTime = abstractCallback.getTime();
        if (callbackTime == null) {
            STEMApp.LOGGER.ERROR("Callback " + abstractCallback.getClass().getSimpleName() + " from " + plugin.getPluginName() + " has no callback time");
            this.callbackListeners.remove(abstractCallback);
            return;
        }
        TaskMeta taskMeta;

        Runnable runnable = () -> callMethod(abstractCallback, plugin);
        if (!callbackTime.isCron) {
            if (callbackTime.fixedTask) {
                taskMeta = STEMApp.core().getScheduler().runFixedScheduler(plugin, runnable, callbackTime.days, callbackTime.hours, callbackTime.minutes, callbackTime.daily);
            } else {
                taskMeta = STEMApp.core().getScheduler().runRepeatScheduler(plugin, runnable, callbackTime.delay, callbackTime.period, callbackTime.timeUnit);
            }
        } else {
            taskMeta = STEMApp.core().getScheduler().runAsCronTask(plugin, runnable, callbackTime.cronTask);
        }
        abstractCallback.setIDs(taskMeta.getTaskId());
        if (taskMeta.isCanceled()) {
            this.callbackListeners.remove(abstractCallback);
            STEMApp.LOGGER.ERROR("Callback " + abstractCallback.getClass().getSimpleName() + " from " + plugin.getPluginName() + " could not be scheduled");
            return;
        }
        STEMApp.LOGGER.DEBUG("Callback register for " + plugin.getPluginName() + " with taskId :" + taskMeta.taskId);
    }

    private void callMethod(AbstractCallback abstractCallback, STEMPlugin plugin) {
        abstractCallback.operation();

        AbstractOperation abstractOperation;
        while ((abstractOperation = abstractCallback.operationData.pollFirst()) != null) {
            STEMApp.LOGGER.DEBUG("Callback operation for " + plugin.getPluginName());
            AbstractOperation operation = abstractOperation;

            STEMApp.core().getScheduler().runTask(plugin, () -> {
                OperationOutput operationOutput = operation.runOperation();
                abstractCallback.callback(operationOutput);
                if (!STEMApp.core().getScheduler().isTask(abstractCallback.taskId)) {
                    this.callbackListeners.remove(abstractCallback);
                    STEMApp.LOGGER.DEBUG("Disable Callback from " + plugin.getPluginName() + " with taskId " + abstractCallback.taskId);
                }
            });
        }
    }
}
