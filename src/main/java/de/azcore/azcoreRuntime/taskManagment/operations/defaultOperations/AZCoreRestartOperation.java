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

package de.azcore.azcoreRuntime.taskManagment.operations.defaultOperations;

import de.azcore.azcoreRuntime.taskManagment.operations.AbstractOperation;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationOutput;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AZCoreRestartOperation extends AbstractOperation {

    @Override
    public OperationOutput runOperation() {
        OperationOutput operationOutput = new OperationOutput(this);
        try {
            String command = "service azcore restart";
            String[] cmd = {"/bin/sh", "-c", command};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor(5, TimeUnit.SECONDS);
            operationOutput.setExit(p.exitValue());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            operationOutput.setExit(-1);
        }
        return operationOutput;
    }
}
