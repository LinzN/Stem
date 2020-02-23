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
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import de.linzn.openJL.pairs.Pair;
import org.json.JSONObject;

import java.util.LinkedList;

public abstract class AbstractCallback {

    LinkedList<Pair<TaskOperation, Object>> operationData;
    private long taskId;

    public AbstractCallback() {
        this.operationData = new LinkedList<>();
    }

    void setIDs(long taskId) {
        this.taskId = taskId;
    }

    public abstract void methodToCall();

    public abstract void callback(Object object);

    public void disable() {
        AZCoreRuntimeApp.getInstance().getScheduler().cancelTask(taskId);
        AZCoreRuntimeApp.getInstance().getCallBackService().removeFromList(this);
    }

    public void addOperationData(TaskOperation taskOperation, JSONObject data) {
        this.operationData.add(new Pair<>(taskOperation, data));
    }

    public abstract CallbackTime getTime();

}
