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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        try {

            StringBuilder command = new StringBuilder();
            if (useSSH) {
                command.append("ssh ").append(sshUser).append("@").append(sshHost).append(" -p ").append(sshPort).append(" -C '");
            }

            command.append(scriptCommand);

            if (useSSH) {
                command.append("'");
            }

            String[] cmd = {"/bin/sh", "-c", command.toString()};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor(60, TimeUnit.MINUTES);
            if (useOutput) {
                List<String> lines = new ArrayList<>();
                BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = b.readLine()) != null) {
                    lines.add(line);
                }
                operationOutput.setData(lines);
            }
            operationOutput.setExit(p.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            operationOutput.setExit(-1);
        }
        return operationOutput;
    }

    public void setUseSSH(boolean useSSH) {
        this.useSSH = useSSH;
    }

    public void setSshHost(String sshHost) {
        this.sshHost = sshHost;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public void setScriptCommand(String scriptCommand) {
        this.scriptCommand = scriptCommand;
    }

    public void setUseOutput(boolean useOutput) {
        this.useOutput = useOutput;
    }

    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }

    public boolean isUseOutput() {
        return useOutput;
    }

    public boolean isUseSSH() {
        return useSSH;
    }

    public int getSshPort() {
        return sshPort;
    }

    public String getSshHost() {
        return sshHost;
    }

    public String getSshUser() {
        return sshUser;
    }

    public String getScriptCommand() {
        return scriptCommand;
    }
}
