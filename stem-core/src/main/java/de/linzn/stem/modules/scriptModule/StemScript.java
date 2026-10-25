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
import de.linzn.stem.utils.ProcessOutput;

import java.io.File;
import java.io.IOException;
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
    private volatile Process process;
    private volatile ProcessOutput processOutput;

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
        if (this.process != null) {
            throw new ScriptException("Script already started!");
        }
        for (String requiredParameter : requiredParameters) {
            if (!this.scriptParameters.containsKey(requiredParameter)) {
                throw new ScriptException("Missing required parameter! : " + requiredParameter);
            }
        }
        /* Arguments are passed as a list, never through a shell, so values can not inject commands */
        ArrayList<String> commandList = new ArrayList<>();
        commandList.add("/bin/bash");
        commandList.add(scriptFile.getAbsolutePath());
        for (Map.Entry<String, String> parameter : this.scriptParameters.entrySet()) {
            commandList.add("-" + parameter.getKey());
            commandList.add(parameter.getValue());
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.redirectErrorStream(true);
        try {
            this.process = processBuilder.start();
        } catch (IOException e) {
            throw new ScriptException(e.getMessage());
        }
        this.processOutput = ProcessOutput.drain(this.process, scriptFile.getName());
        this.scriptManager.stemScripts.add(this);
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
                this.collectOutput();
            } else {
                this.destroy();
                throw new ScriptTimeoutException();
            }
        } catch (InterruptedException e) {
            this.destroy();
            Thread.currentThread().interrupt();
            throw new ScriptTimeoutException();
        } finally {
            this.scriptManager.stemScripts.remove(this);
            ProcessOutput.closeStreams(this.process);
        }
    }

    private void collectOutput() {
        if (this.processOutput != null) {
            this.processOutput.awaitCompletion(10, TimeUnit.SECONDS);
            synchronized (this.output_lines) {
                this.output_lines.clear();
                this.output_lines.addAll(this.processOutput.getLines());
            }
        }
    }

    public int exitCode() throws ScriptNotStartedException {
        if (this.process == null) {
            throw new ScriptNotStartedException();
        }
        return process.exitValue();
    }

    void destroy() {
        Process runningProcess = this.process;
        if (runningProcess == null) {
            return;
        }
        runningProcess.descendants().forEach(ProcessHandle::destroyForcibly);
        runningProcess.destroyForcibly();
        try {
            runningProcess.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.collectOutput();
    }

    /**
     * stdout and stderr are merged into the output lines; this list is kept for compatibility and stays empty.
     */
    public List<String> getOutput_lines() {
        synchronized (this.output_lines) {
            return new ArrayList<>(output_lines);
        }
    }

    public List<String> getError_lines() {
        return error_lines;
    }

}
