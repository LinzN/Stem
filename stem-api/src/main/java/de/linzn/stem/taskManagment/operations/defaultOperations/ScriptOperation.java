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
import de.linzn.stem.api.script.ScriptFailedException;
import de.linzn.stem.api.script.ScriptResult;
import de.linzn.stem.modules.scriptModule.exceptions.InvalidScriptException;
import de.linzn.stem.modules.scriptModule.exceptions.ScriptNotFoundException;
import de.linzn.stem.modules.scriptModule.exceptions.ScriptNotStartedException;
import de.linzn.stem.modules.scriptModule.exceptions.ScriptTimeoutException;
import de.linzn.stem.taskManagment.operations.AbstractOperation;
import de.linzn.stem.taskManagment.operations.OperationOutput;
import org.json.JSONObject;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Runs a STEM script as callback operation.
 * <p>
 * Exit codes on failure: 1 script not found, 2 invalid script, 3 timeout, 4 not started, 5 other error.
 */
public class ScriptOperation extends AbstractOperation {

    private static final Duration TIMEOUT = Duration.ofMinutes(60);

    private final String scriptName;
    private final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();

    public ScriptOperation(String scriptName) {
        this.scriptName = scriptName;
    }

    private static int exitCodeFor(Throwable cause) {
        if (cause instanceof ScriptNotFoundException) {
            return 1;
        } else if (cause instanceof InvalidScriptException) {
            return 2;
        } else if (cause instanceof ScriptTimeoutException) {
            return 3;
        } else if (cause instanceof ScriptNotStartedException) {
            return 4;
        }
        return 5;
    }

    @Override
    public OperationOutput runOperation() {
        OperationOutput operationOutput = new OperationOutput(this);
        try {
            ScriptResult result = Stem.api().scripts().run(this.scriptName, new LinkedHashMap<>(this.parameters), TIMEOUT);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("outputLines", result.output());
            jsonObject.put("errorLines", List.of());
            operationOutput.setExit(result.exitCode());
            operationOutput.setData(jsonObject);
        } catch (ScriptFailedException e) {
            if (Stem.isAvailable()) {
                Stem.api().logger().error(e.getMessage());
            }
            operationOutput.setExit(exitCodeFor(e.getCause()));
        } catch (RuntimeException e) {
            if (Stem.isAvailable()) {
                Stem.api().logger().error(e);
            }
            operationOutput.setExit(5);
        }
        return operationOutput;
    }

    public void addParameter(String parameter, String value) {
        parameters.put(parameter, value);
    }

    public String getParameterValue(String parameterName) {
        return this.parameters.get(parameterName);
    }
}
