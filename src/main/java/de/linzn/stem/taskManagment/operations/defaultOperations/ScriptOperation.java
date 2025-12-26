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
import de.linzn.stem.modules.scriptModule.StemScript;
import de.linzn.stem.modules.scriptModule.exceptions.*;
import de.linzn.stem.taskManagment.operations.AbstractOperation;
import de.linzn.stem.taskManagment.operations.OperationOutput;
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
            StemScript stemScript = STEMApp.getInstance().getScriptManager().getStemScript(this.scriptName);
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
            STEMApp.LOGGER.ERROR(e);
            operationOutput.setExit(1);
        } catch (InvalidScriptException e) {
            STEMApp.LOGGER.ERROR(e);
            operationOutput.setExit(2);
        } catch (ScriptTimeoutException e) {
            STEMApp.LOGGER.ERROR(e);
            operationOutput.setExit(3);
        } catch (ScriptNotStartedException e) {
            STEMApp.LOGGER.ERROR(e);
            operationOutput.setExit(4);
        } catch (ScriptException e) {
            STEMApp.LOGGER.ERROR(e);
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
