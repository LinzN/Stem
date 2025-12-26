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

package de.linzn.stem.modules.scriptModule;

import de.linzn.stem.modules.scriptModule.exceptions.ScriptException;
import de.linzn.stem.modules.scriptModule.exceptions.ScriptNotStartedException;
import de.linzn.stem.modules.scriptModule.exceptions.ScriptTimeoutException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class StemScript {

    private final ScriptManager scriptManager;
    private final File scriptFile;
    private final List<String> requiredParameters;
    private final Map<String, String> scriptParameters;
    private final List<String> output_lines;
    private final List<String> error_lines;
    private Process process;

    public StemScript(ScriptManager scriptManager, File scriptFile, List<String> requiredParameters) {
        this.scriptManager = scriptManager;
        this.scriptFile = scriptFile;
        this.requiredParameters = requiredParameters;
        this.scriptParameters = new LinkedHashMap<>();
        this.output_lines = new ArrayList<>();
        this.error_lines = new ArrayList<>();
    }

    public void addScriptParameter(String parameterName, Object parameterValue) {
        this.scriptParameters.put(parameterName.toLowerCase(), parameterValue.toString());
    }

    public File getScriptFile() {
        return scriptFile;
    }

    public void start() throws ScriptException {
        for (String requiredParameter : requiredParameters) {
            if (!this.scriptParameters.containsKey(requiredParameter)) {
                throw new ScriptException("Missing required parameter! : " + requiredParameter);
            }
        }

        ArrayList<String> commandList = new ArrayList<>();
        commandList.add("/bin/bash");
        commandList.add(scriptFile.getAbsolutePath());

        for (String scriptParameterName : this.scriptParameters.keySet()) {
            commandList.add("-" + scriptParameterName);
            commandList.add(this.scriptParameters.get(scriptParameterName));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.redirectErrorStream(true);

        try {
            this.process = processBuilder.start();
            this.scriptManager.stemScripts.add(this);
        } catch (IOException e) {
            throw new ScriptException(e.getMessage());
        }
    }

    public void await() throws ScriptNotStartedException, ScriptTimeoutException {
        await(60, TimeUnit.MINUTES);
    }

    public void await(int timeout, TimeUnit timeUnit) throws ScriptNotStartedException, ScriptTimeoutException {
        if (this.process == null) {
            throw new ScriptNotStartedException();
        }

        try {
            if (process.waitFor(timeout, timeUnit)) {
                this.scriptManager.stemScripts.remove(this);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output_lines.add(line);
                    }
                } catch (IOException ignored) {
                }
            } else {
                process.destroyForcibly();
                this.scriptManager.stemScripts.remove(this);
                throw new ScriptTimeoutException();
            }
        } catch (InterruptedException ignored) {
        }
    }

    public int exitCode() throws ScriptNotStartedException {
        if (this.process == null) {
            throw new ScriptNotStartedException();
        }
        return process.exitValue();
    }

    void destroy() {
        this.process.destroyForcibly();
    }

    public List<String> getOutput_lines() {
        return output_lines;
    }

    public List<String> getError_lines() {
        return error_lines;
    }

}
