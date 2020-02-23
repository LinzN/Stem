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

public class CallbackService {
    private HashMap<AbstractCallback, AZPlugin> callbackListeners;

    CallbackService() {
        this.callbackListeners = new HashMap<>();
    }

    public void registerCallbackListener(AbstractCallback abstractCallback, AZPlugin azPlugin) {
        this.callbackListeners.put(abstractCallback, azPlugin);
        this.enableCallbackListener(abstractCallback, azPlugin);
    }

    public void unregisterCallbackListener(AbstractCallback abstractCallback) {
        abstractCallback.disable();
    }

    public void unregisterCallbackListeners(AZPlugin azPlugin) {
        for (Iterator<AbstractCallback> iterator = this.callbackListeners.keySet().iterator(); iterator.hasNext(); ) {
            AbstractCallback abstractCallback = iterator.next();
            AZPlugin azPlugin1 = this.callbackListeners.get(abstractCallback);
            if (azPlugin == azPlugin1) {
                abstractCallback.disable();
            }
        }
    }

    void removeFromList(AbstractCallback abstractCallback) {
        this.callbackListeners.remove(abstractCallback);
    }

    private void enableCallbackListener(AbstractCallback abstractCallback, AZPlugin plugin) {
        CallbackTime callbackTime = abstractCallback.getTime();
        AZTask azTask;

        Runnable runnable = () -> callMethod(abstractCallback, plugin);

        if (callbackTime.fixedTask) {
            azTask = AZCoreRuntimeApp.getInstance().getScheduler().runFixedScheduler(plugin, runnable, callbackTime.days, callbackTime.hours, callbackTime.minutes, callbackTime.daily);
        } else {
            azTask = AZCoreRuntimeApp.getInstance().getScheduler().runRepeatScheduler(plugin, runnable, callbackTime.delay, callbackTime.period, callbackTime.timeUnit);
        }

        abstractCallback.setIDs(azTask.getTaskId());
    }

    private void callMethod(AbstractCallback abstractCallback, AZPlugin plugin) {
        abstractCallback.methodToCall();

        while (!abstractCallback.operationData.isEmpty()) {
            Pair<TaskOperation, Object> pair = abstractCallback.operationData.removeFirst();

            AZCoreRuntimeApp.getInstance().getScheduler().runTask(plugin, () -> {
                Object object = pair.getKey().runOperation(pair.getValue());
                abstractCallback.callback(object);
            });
        }
    }
}
