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

import de.linzn.stem.api.Stem;
import de.linzn.stem.taskManagment.operations.AbstractOperation;
import de.linzn.stem.taskManagment.operations.OperationOutput;

import de.linzn.stem.utils.ProcessOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShellOperation extends AbstractOperation {
    private boolean useSSH;
    private String sshHost;
    private int sshPort;
    private String sshUser;
    private String scriptCommand;
    private boolean useOutput;

    @Override
    public OperationOutput runOperation() {
        OperationOutput operationOutput = new OperationOutput(this);
        if (scriptCommand == null || scriptCommand.isBlank()) {
            logError("ShellOperation without command");
            operationOutput.setExit(-1);
            return operationOutput;
        }
        List<String> command = new ArrayList<>();
        if (useSSH) {
            if (!isSafeSshArgument(sshUser) || !isSafeSshArgument(sshHost)) {
                logError("ShellOperation with invalid ssh user or host");
                operationOutput.setExit(-1);
                return operationOutput;
            }
            /* No local shell: the remote command is passed to ssh as a single argument, so quotes in it
             * can never break out into a local command. BatchMode prevents hanging on password prompts. */
            command.add("ssh");
            command.add("-p");
            command.add(String.valueOf(sshPort));
            command.add("-C");
            command.add("-o");
            command.add("BatchMode=yes");
            command.add(sshUser + "@" + sshHost);
            command.add(scriptCommand);
        } else {
            command.add("/bin/sh");
            command.add("-c");
            command.add(scriptCommand);
        }

        Process p = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            p = processBuilder.start();
            ProcessOutput processOutput = ProcessOutput.drain(p, "ShellOperation");
            if (!p.waitFor(60, TimeUnit.MINUTES)) {
                logError("ShellOperation timed out: " + (useSSH ? sshHost : "local"));
                p.descendants().forEach(ProcessHandle::destroyForcibly);
                p.destroyForcibly();
                operationOutput.setExit(-1);
                return operationOutput;
            }
            processOutput.awaitCompletion(10, TimeUnit.SECONDS);
            if (useOutput) {
                operationOutput.setData(processOutput.getLines());
            }
            operationOutput.setExit(p.exitValue());
        } catch (IOException e) {
            logError(e);
            operationOutput.setExit(-1);
        } catch (InterruptedException e) {
            if (p != null) {
                p.destroyForcibly();
            }
            Thread.currentThread().interrupt();
            operationOutput.setExit(-1);
        } finally {
            ProcessOutput.closeStreams(p);
        }
        return operationOutput;
    }

    private static void logError(Object message) {
        if (!Stem.isAvailable()) {
            return;
        }
        if (message instanceof Throwable throwable) {
            Stem.api().logger().error(throwable);
        } else {
            Stem.api().logger().error(String.valueOf(message));
        }
    }

    private static boolean isSafeSshArgument(String value) {
        return value != null && !value.isBlank() && !value.startsWith("-") && value.chars().noneMatch(Character::isWhitespace);
    }

    public boolean isUseOutput() {
        return useOutput;
    }

    public void setUseOutput(boolean useOutput) {
        this.useOutput = useOutput;
    }

    public boolean isUseSSH() {
        return useSSH;
    }

    public void setUseSSH(boolean useSSH) {
        this.useSSH = useSSH;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public String getSshHost() {
        return sshHost;
    }

    public void setSshHost(String sshHost) {
        this.sshHost = sshHost;
    }

    public String getSshUser() {
        return sshUser;
    }

    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }

    public String getScriptCommand() {
        return scriptCommand;
    }

    public void setScriptCommand(String scriptCommand) {
        this.scriptCommand = scriptCommand;
    }
}
