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
import org.json.JSONObject;

import java.util.LinkedList;

public abstract class AbstractCallback {

    LinkedList<Object> callbackData;
    LinkedList<Pair<TaskOperation, Object>> operationData;
    private long taskId;
    private long callbackTaskId;
    private AZPlugin azPlugin;

    public AbstractCallback(AZPlugin azPlugin) {
        this.azPlugin = azPlugin;
        this.operationData = new LinkedList<>();
        this.callbackData = new LinkedList<>();
    }

    void setIDs(long taskId, long callbackTaskId) {
        this.taskId = taskId;
        this.callbackTaskId = callbackTaskId;
    }

    public abstract void methodToCall();

    public abstract void callback(Object object);

    public void disable() {
        AZCoreRuntimeApp.getInstance().getScheduler().cancelTask(taskId);
        AZCoreRuntimeApp.getInstance().getScheduler().cancelTask(callbackTaskId);
        AZCoreRuntimeApp.getInstance().getCallBackService().removeFromList(this);
    }

    public AZPlugin getAZPlugin() {
        return this.azPlugin;
    }

    public void addOperationData(TaskOperation taskOperation, JSONObject data) {
        this.operationData.add(new Pair<>(taskOperation, data));
    }

    public abstract CallbackTime getTime();

}
