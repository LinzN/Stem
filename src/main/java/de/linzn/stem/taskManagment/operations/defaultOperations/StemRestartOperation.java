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
import de.linzn.stem.utils.ProcessOutput;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class StemRestartOperation extends AbstractOperation {

    @Override
    public OperationOutput runOperation() {
        OperationOutput operationOutput = new OperationOutput(this);
        Process p;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", "service stem restart");
            processBuilder.redirectErrorStream(true);
            p = processBuilder.start();
            ProcessOutput.drain(p, "StemRestart");
            if (p.waitFor(5, TimeUnit.SECONDS)) {
                operationOutput.setExit(p.exitValue());
                ProcessOutput.closeStreams(p);
            } else {
                /* The restart usually stops this JVM before the command returns */
                operationOutput.setExit(0);
            }
        } catch (IOException e) {
            STEMApp.LOGGER.ERROR(e);
            operationOutput.setExit(-1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            operationOutput.setExit(-1);
        }
        return operationOutput;
    }
}
