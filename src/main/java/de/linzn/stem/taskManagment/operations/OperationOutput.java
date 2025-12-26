/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.stem.taskManagment.operations;

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
