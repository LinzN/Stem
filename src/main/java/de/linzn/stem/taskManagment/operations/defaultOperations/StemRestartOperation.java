/*
 * Copyright (c) 2026 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.stem.taskManagment.operations.defaultOperations;

import de.linzn.stem.STEMApp;
import de.linzn.stem.taskManagment.operations.AbstractOperation;
import de.linzn.stem.taskManagment.operations.OperationOutput;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class StemRestartOperation extends AbstractOperation {

    @Override
    public OperationOutput runOperation() {
        OperationOutput operationOutput = new OperationOutput(this);
        try {
            String command = "service stem restart";
            String[] cmd = {"/bin/sh", "-c", command};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor(5, TimeUnit.SECONDS);
            operationOutput.setExit(p.exitValue());

        } catch (IOException | InterruptedException e) {
            STEMApp.LOGGER.ERROR(e);
            operationOutput.setExit(-1);
        }
        return operationOutput;
    }
}
