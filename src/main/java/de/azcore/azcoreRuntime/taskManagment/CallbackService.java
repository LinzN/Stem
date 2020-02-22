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
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import de.linzn.openJL.pairs.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

class CallbackService {
    private HashMap<AbstractCallback, AZPlugin> callbackListeners;

    CallbackService() {
        this.callbackListeners = new HashMap<>();
    }

    public void registerCallbackListener(AbstractCallback abstractCallback, AZPlugin azPlugin) {
        this.callbackListeners.put(abstractCallback, azPlugin);
        this.enableCallbackListener(abstractCallback);
    }

    public void unregisterCallbackListener(AbstractCallback abstractCallback) {
        abstractCallback.disable();
        this.callbackListeners.remove(abstractCallback);
    }

    public void unregisterCallbackListeners(AZPlugin azPlugin) {
        for (Iterator<AbstractCallback> iterator = this.callbackListeners.keySet().iterator(); iterator.hasNext(); ) {
            AbstractCallback abstractCallback = iterator.next();
            AZPlugin azPlugin1 = this.callbackListeners.get(abstractCallback);
            if (azPlugin == azPlugin1) {
                abstractCallback.disable();
                iterator.remove();
            }
        }
    }

    private void enableCallbackListener(AbstractCallback abstractCallback) {
        AZPlugin plugin = abstractCallback.getAZPlugin();

        CallbackTime callbackTime = abstractCallback.getTime();
        AZTask taskId;

        Runnable task = () -> callMethod(abstractCallback);

        if (callbackTime.fixedTask) {
            taskId = AZCoreRuntimeApp.getInstance().getScheduler().runFixedScheduler(plugin, task, callbackTime.days, callbackTime.hours, callbackTime.minutes, callbackTime.daily);
        } else {
            taskId = AZCoreRuntimeApp.getInstance().getScheduler().runRepeatScheduler(plugin, task, callbackTime.delay, callbackTime.period, callbackTime.timeUnit);
        }

        AZTask callbackId = AZCoreRuntimeApp.getInstance().getScheduler().runRepeatScheduler(plugin, () -> {
            try {
                if (!abstractCallback.callbackData.isEmpty()) {
                    Object object = abstractCallback.callbackData.removeFirst();
                    abstractCallback.callback(object);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 100, 100, TimeUnit.MILLISECONDS);

        abstractCallback.setIDs(taskId.getTaskId(), callbackId.getTaskId());

    }

    private void callMethod(AbstractCallback abstractCallback) {
        abstractCallback.methodToCall();

        while (!abstractCallback.operationData.isEmpty()) {
            Pair<TaskOperation, Object> pair = abstractCallback.operationData.removeFirst();

            Object object = pair.getKey().runOperation(pair.getValue());
            abstractCallback.callback(object);
        }
    }
}
