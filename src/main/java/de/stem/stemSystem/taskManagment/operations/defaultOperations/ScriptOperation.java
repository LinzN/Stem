/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem.taskManagment.operations.defaultOperations;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.scriptModule.StemScript;
import de.stem.stemSystem.modules.scriptModule.exceptions.*;
import de.stem.stemSystem.taskManagment.operations.AbstractOperation;
import de.stem.stemSystem.taskManagment.operations.OperationOutput;
import org.json.JSONObject;

import java.util.LinkedHashMap;

public class ScriptOperation extends AbstractOperation {

    private final String scriptName;
    private final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();

    public ScriptOperation(String scriptName) {
        this.scriptName = scriptName;
    }

    @Override
    public OperationOutput runOperation() {
        OperationOutput operationOutput = new OperationOutput(this);
        JSONObject jsonObject = new JSONObject();
        try {
            StemScript stemScript = STEMSystemApp.getInstance().getScriptManager().getStemScript(this.scriptName);
            for (String parameterName : parameters.keySet()) {
                stemScript.addScriptParameter(parameterName, parameters.get(parameterName));
            }
            stemScript.start();
            stemScript.await();

            jsonObject.put("outputLines", stemScript.getOutput_lines());
            jsonObject.put("errorLines", stemScript.getError_lines());
            operationOutput.setExit(stemScript.exitCode());
            operationOutput.setData(jsonObject);
        } catch (ScriptNotFoundException e) {
            STEMSystemApp.LOGGER.ERROR(e);
            operationOutput.setExit(1);
        } catch (InvalidScriptException e) {
            STEMSystemApp.LOGGER.ERROR(e);
            operationOutput.setExit(2);
        } catch (ScriptTimeoutException e) {
            STEMSystemApp.LOGGER.ERROR(e);
            operationOutput.setExit(3);
        } catch (ScriptNotStartedException e) {
            STEMSystemApp.LOGGER.ERROR(e);
            operationOutput.setExit(4);
        } catch (ScriptException e) {
            STEMSystemApp.LOGGER.ERROR(e);
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
