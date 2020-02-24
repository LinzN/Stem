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

import de.azcore.azcoreRuntime.taskManagment.operations.OperationSettings;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LinuxOperations {

    public static TaskOperation run_linux_shell = operationSettings -> {
        OperationSettings returnData = new OperationSettings();
        returnData.addSetting("input.settings", operationSettings);
        try {

            StringBuilder command = new StringBuilder();
            boolean useSSH = operationSettings.getBooleanSetting("ssh.use");
            if (useSSH) {
                String sshUser = operationSettings.getStringSetting("ssh.user");
                String host = operationSettings.getStringSetting("ssh.host");
                int port = operationSettings.getIntSetting("ssh.port");
                command.append("ssh ").append(sshUser).append("@").append(host).append(" -p ").append(port).append(" -C '");
            }

            String script = operationSettings.getStringSetting("command.script");
            command.append(script);

            if (useSSH) {
                command.append("'");
            }

            boolean withOutput = operationSettings.getBooleanSetting("output.use");

            String[] cmd = {"/bin/sh", "-c", command.toString()};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor(60, TimeUnit.MINUTES);
            if (withOutput) {
                List<String> lines = new ArrayList<>();
                BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = b.readLine()) != null) {
                    lines.add(line);
                }
                returnData.addSetting("output", lines);
            }
            returnData.addSetting("exit", p.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            returnData.addSetting("exit", -1);
        }
        return returnData;
    };
}
