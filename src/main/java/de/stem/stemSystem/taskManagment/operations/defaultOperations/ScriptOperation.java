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

import de.linzn.openJL.pairs.ImmutablePair;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.scriptModule.StemScript;
import de.stem.stemSystem.modules.scriptModule.exceptions.InvalidScriptException;
import de.stem.stemSystem.modules.scriptModule.exceptions.ScriptNotFoundException;
import de.stem.stemSystem.taskManagment.operations.AbstractOperation;
import de.stem.stemSystem.taskManagment.operations.OperationOutput;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ScriptOperation extends AbstractOperation {

    private final String scriptName;
    private final ArrayList<ImmutablePair<String, String>> parameters = new ArrayList<>();

    public ScriptOperation(String scriptName) {
        this.scriptName = scriptName;
    }

    @Override
    public OperationOutput runOperation() {
        OperationOutput operationOutput = new OperationOutput(this);
        JSONObject jsonObject = new JSONObject();
        try {
            StemScript stemScript = STEMSystemApp.getInstance().getScriptManager().getStemScript(this.scriptName);
            for (ImmutablePair<String, String> parameter : parameters) {
                stemScript.addScriptParameter(parameter.getLeft(), parameter.getRight());
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
        } catch (IOException | InterruptedException e) {
            STEMSystemApp.LOGGER.ERROR(e);
            operationOutput.setExit(3);
        }
        return operationOutput;
    }

    public void addParameter(String parameter, String value) {
        parameters.add(new ImmutablePair<>(parameter, value));
    }
}
