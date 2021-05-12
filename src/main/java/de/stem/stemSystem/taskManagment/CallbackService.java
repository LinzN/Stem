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

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;
import de.stem.stemSystem.taskManagment.operations.AbstractOperation;
import de.stem.stemSystem.taskManagment.operations.OperationOutput;

import java.util.HashMap;
import java.util.Iterator;

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
        STEMSystemApp.getInstance().getScheduler().cancelTask(abstractCallback.taskId);
        this.callbackListeners.remove(abstractCallback);
        STEMSystemApp.LOGGER.DEBUG("Callback unregister: " + abstractCallback.getClass().getSimpleName());
    }

    public void unregisterCallbackListeners(STEMPlugin stemPlugin) {
        for (Iterator<AbstractCallback> iterator = this.callbackListeners.keySet().iterator(); iterator.hasNext(); ) {
            AbstractCallback abstractCallback = iterator.next();
            STEMPlugin stemPlugin1 = this.callbackListeners.get(abstractCallback);
            if (stemPlugin == stemPlugin1) {
                STEMSystemApp.getInstance().getScheduler().cancelTask(abstractCallback.taskId);
                this.callbackListeners.remove(abstractCallback);
                STEMSystemApp.LOGGER.DEBUG("Callback unregister: " + abstractCallback.getClass().getSimpleName() + " from " + stemPlugin.getPluginName());
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
        if (callbackTime.fixedTask) {
            taskMeta = STEMSystemApp.getInstance().getScheduler().runFixedScheduler(plugin, runnable, callbackTime.days, callbackTime.hours, callbackTime.minutes, callbackTime.daily);
        } else {
            taskMeta = STEMSystemApp.getInstance().getScheduler().runRepeatScheduler(plugin, runnable, callbackTime.delay, callbackTime.period, callbackTime.timeUnit);
        }
        STEMSystemApp.LOGGER.DEBUG("Callback register for " + plugin.getPluginName() + " with taskId :" + taskMeta.taskId);
        abstractCallback.setIDs(taskMeta.getTaskId());
    }

    private void callMethod(AbstractCallback abstractCallback, STEMPlugin plugin) {
        abstractCallback.operation();

        while (!abstractCallback.operationData.isEmpty()) {
            STEMSystemApp.LOGGER.DEBUG("Callback operation for " + plugin.getPluginName());
            AbstractOperation abstractOperation = abstractCallback.operationData.removeFirst();

            STEMSystemApp.getInstance().getScheduler().runTask(plugin, () -> {
                OperationOutput operationOutput = abstractOperation.runOperation();
                abstractCallback.callback(operationOutput);
                if (!STEMSystemApp.getInstance().getScheduler().isTask(abstractCallback.taskId)) {
                    this.callbackListeners.remove(abstractCallback);
                    STEMSystemApp.LOGGER.DEBUG("Disable Callback from " + plugin.getPluginName() + " with taskId " + abstractCallback.taskId);
                }
            });
        }
    }
}
