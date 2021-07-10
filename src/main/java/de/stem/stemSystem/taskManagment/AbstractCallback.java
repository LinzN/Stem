/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem.taskManagment;

import de.stem.stemSystem.taskManagment.operations.AbstractOperation;
import de.stem.stemSystem.taskManagment.operations.OperationOutput;

import java.util.LinkedList;

public abstract class AbstractCallback {

    public long taskId;
    LinkedList<AbstractOperation> operationData;

    public AbstractCallback() {
        this.operationData = new LinkedList<>();
    }

    void setIDs(long taskId) {
        this.taskId = taskId;
    }

    public abstract void operation();

    public abstract void callback(OperationOutput operationOutput);

    public void addOperationData(AbstractOperation abstractOperation) {
        this.operationData.add(abstractOperation);
    }

    public abstract CallbackTime getTime();

}
