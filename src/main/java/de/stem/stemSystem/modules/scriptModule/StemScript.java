package de.stem.stemSystem.modules.scriptModule;

import de.linzn.openJL.pairs.ImmutablePair;
import de.stem.stemSystem.modules.scriptModule.exceptions.ScriptNotStartedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class StemScript {

    private final ScriptManager scriptManager;
    private final File scriptFile;
    private final ArrayList<ImmutablePair<String, String>> scriptParameters;
    private Process process;
    private final List<String> output_lines;
    private final List<String> error_lines;

    public StemScript(ScriptManager scriptManager, File scriptFile) {
        this.scriptManager = scriptManager;
        this.scriptFile = scriptFile;
        this.scriptParameters = new ArrayList<>();
        this.output_lines = new ArrayList<>();
        this.error_lines = new ArrayList<>();
    }

    public void addScriptParameter(String parameterName, Object parameterValue) {
        this.scriptParameters.add(new ImmutablePair<>(parameterName, parameterValue.toString()));
    }

    public File getScriptFile() {
        return scriptFile;
    }

    public void start() throws IOException {
        ArrayList<String> commandList = new ArrayList<>();
        commandList.add("/bin/bash");
        commandList.add(scriptFile.getAbsolutePath());

        this.scriptParameters.forEach(parameter -> {
            commandList.add("-" + parameter.getLeft());
            commandList.add(parameter.getRight());
        });
        this.process = Runtime.getRuntime().exec(commandList.toArray(String[]::new));
        this.scriptManager.stemScripts.add(this);
    }

    public boolean await() throws InterruptedException, ScriptNotStartedException {
        return await(60, TimeUnit.MINUTES);
    }

    public boolean await(int timeout, TimeUnit timeUnit) throws InterruptedException, ScriptNotStartedException {
        if (this.process == null) {
            throw new ScriptNotStartedException();
        }

        if (process.waitFor(timeout, timeUnit)) {
            this.scriptManager.stemScripts.remove(this);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output_lines.add(line);
                }
            } catch (IOException ignored) {
            }

            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    error_lines.add(line);
                }
            } catch (IOException ignored) {
            }

            return true;
        } else {
            process.destroyForcibly();
            this.scriptManager.stemScripts.remove(this);
            return false;
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
