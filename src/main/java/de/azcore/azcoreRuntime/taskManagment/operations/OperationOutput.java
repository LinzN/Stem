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

package de.azcore.azcoreRuntime.taskManagment.operations;

public class OperationOutput {
    private int exit;
    private Object data;
    private AbstractOperation abstractOperation;

    public OperationOutput(AbstractOperation abstractOperation) {
        this.exit = -1;
        this.abstractOperation = abstractOperation;
    }

    public AbstractOperation getAbstractOperation() {
        return abstractOperation;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getExit() {
        return exit;
    }

    public void setExit(int exit) {
        this.exit = exit;
    }
}
