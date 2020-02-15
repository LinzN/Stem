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

package de.azcore.azcoreRuntime.internal.containers;

import org.json.JSONObject;

public class TaskContainer {

    private TaskOperation taskOperation;
    private JSONObject jsonObject;

    public TaskContainer(TaskOperation taskOperation, JSONObject jsonObject) {
        this.taskOperation = taskOperation;
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public TaskOperation getTaskOperation() {
        return taskOperation;
    }
}
