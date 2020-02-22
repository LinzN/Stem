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

import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class LinuxOperations {

    public static TaskOperation run_linux_shell = object -> {
        JSONObject answerObject = new JSONObject();
        answerObject.put("requestData", object);
        try {
            JSONObject jsonObject = (JSONObject) object;
            StringBuilder command = new StringBuilder();
            boolean useSSH = jsonObject.getJSONObject("ssh").getBoolean("useSSH");
            if (useSSH) {
                String sshUser = jsonObject.getJSONObject("ssh").getString("user");
                String host = jsonObject.getJSONObject("ssh").getString("host");
                int port = jsonObject.getJSONObject("ssh").getInt("port");
                command.append("ssh ").append(sshUser).append("@").append(host).append(" -p ").append(port).append(" -C '");
            }

            boolean isScript = jsonObject.getJSONObject("command").getBoolean("isScript");

            if (isScript) {
                //String[] script = (String[]) jsonObject.getJSONObject("command").get("script");
                //command.append(script);
            } else {
                String script = jsonObject.getJSONObject("command").getString("script");
                command.append(script);
            }
            if (useSSH) {
                command.append("'");
            }

            boolean withOutput = jsonObject.getBoolean("useOutput");

            String[] cmd = {"/bin/sh", "-c", command.toString()};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor(60, TimeUnit.MINUTES);
            if (withOutput) {
                JSONArray jsonArray = new JSONArray();
                BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = b.readLine()) != null) {
                    jsonArray.put(line);
                }
                answerObject.put("output", jsonArray);
            }
            answerObject.put("exitcode", p.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            answerObject.put("exitcode", -1);
        }
        return answerObject;
    };
}
