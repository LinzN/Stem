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
import java.util.HashSet;
import java.util.Set;

public class CallbackService {
    private final HashMap<AbstractCallback, STEMPlugin> callbackListeners;

    CallbackService() {
        this.callbackListeners = new HashMap<>();
    }

    public void registerCallbackListener(AbstractCallback abstractCallback, STEMPlugin stemPlugin) {
        this.callbackListeners.put(abstractCallback, stemPlugin);
        this.enableCallbackListener(abstractCallback, stemPlugin);
    }

    public void unregisterCallbackListener(AbstractCallback abstractCallback) {
        STEMApp.getInstance().getScheduler().cancelTask(abstractCallback.taskId);
        this.callbackListeners.remove(abstractCallback);
        STEMApp.LOGGER.DEBUG("Callback unregister: " + abstractCallback.getClass().getSimpleName());
    }

    public void unregisterCallbackListeners(STEMPlugin stemPlugin) {
        Set<AbstractCallback> setCopy = new HashSet<>(this.callbackListeners.keySet());
        for (AbstractCallback abstractCallback : setCopy) {
            STEMPlugin stemPlugin1 = this.callbackListeners.get(abstractCallback);
            if (stemPlugin == stemPlugin1) {
                STEMApp.getInstance().getScheduler().cancelTask(abstractCallback.taskId);
                this.callbackListeners.remove(abstractCallback);
                STEMApp.LOGGER.DEBUG("Callback unregister: " + abstractCallback.getClass().getSimpleName() + " from " + stemPlugin.getPluginName());
            }
        }
    }

    public HashMap<AbstractCallback, STEMPlugin> getCallbackListeners() {
        return this.callbackListeners;
    }

    private void enableCallbackListener(AbstractCallback abstractCallback, STEMPlugin plugin) {
        CallbackTime callbackTime = abstractCallback.getTime();
        TaskMeta taskMeta;

        Runnable runnable = () -> callMethod(abstractCallback, plugin);
        if (!callbackTime.isCron) {
            if (callbackTime.fixedTask) {
                taskMeta = STEMApp.getInstance().getScheduler().runFixedScheduler(plugin, runnable, callbackTime.days, callbackTime.hours, callbackTime.minutes, callbackTime.daily);
            } else {
                taskMeta = STEMApp.getInstance().getScheduler().runRepeatScheduler(plugin, runnable, callbackTime.delay, callbackTime.period, callbackTime.timeUnit);
            }
        } else {
            taskMeta = STEMApp.getInstance().getScheduler().runAsCronTask(plugin, runnable, callbackTime.cronTask);
        }
        STEMApp.LOGGER.DEBUG("Callback register for " + plugin.getPluginName() + " with taskId :" + taskMeta.taskId);
        abstractCallback.setIDs(taskMeta.getTaskId());
    }

    private void callMethod(AbstractCallback abstractCallback, STEMPlugin plugin) {
        abstractCallback.operation();

        while (!abstractCallback.operationData.isEmpty()) {
            STEMApp.LOGGER.DEBUG("Callback operation for " + plugin.getPluginName());
            AbstractOperation abstractOperation = abstractCallback.operationData.removeFirst();

            STEMApp.getInstance().getScheduler().runTask(plugin, () -> {
                OperationOutput operationOutput = abstractOperation.runOperation();
                abstractCallback.callback(operationOutput);
                if (!STEMApp.getInstance().getScheduler().isTask(abstractCallback.taskId)) {
                    this.callbackListeners.remove(abstractCallback);
                    STEMApp.LOGGER.DEBUG("Disable Callback from " + plugin.getPluginName() + " with taskId " + abstractCallback.taskId);
                }
            });
        }
    }
}
