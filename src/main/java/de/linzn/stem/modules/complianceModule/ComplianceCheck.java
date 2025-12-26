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

package de.linzn.stem.modules.complianceModule;


import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import de.linzn.stem.taskManagment.AbstractCallback;
import de.linzn.stem.taskManagment.CallbackTime;
import de.linzn.stem.taskManagment.operations.OperationOutput;
import de.linzn.stem.taskManagment.operations.defaultOperations.ScriptOperation;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;


public class ComplianceCheck extends AbstractCallback {

    private final String complianceID;
    private final String scriptName;
    private final String cronTime;
    private final List<Integer> runningCodes;
    private final List<Integer> errorCodes;
    private final STEMPlugin stemPlugin;
    private final STEMApp stemApp;

    public JSONObject status;

    ComplianceCheck(String complianceID, String scriptName, String cronTime, List<Integer> runningCodes, List<Integer> errorCodes, STEMPlugin stemPlugin, STEMApp stemApp) {
        this.complianceID = complianceID;
        this.scriptName = scriptName;
        this.cronTime = cronTime;
        this.runningCodes = runningCodes;
        this.errorCodes = errorCodes;
        this.stemPlugin = stemPlugin;
        this.stemApp = stemApp;
        this.status = new JSONObject();
        this.status.put("complianceID", complianceID);
        this.status.put("scriptName", scriptName);
        this.status.put("statusCode", -1);
        this.status.put("status", "pending");
        this.status.put("checked", false);
        this.status.put("lastCheck", new Date(0));
    }

    @Override
    public void operation() {
        ScriptOperation scriptOperation = new ScriptOperation(scriptName);
        addOperationData(scriptOperation);
    }

    @Override
    public void callback(OperationOutput operationOutput) {
        int exitCode = operationOutput.getExit();
        String statusString = "unknown";

        if(this.runningCodes.contains(exitCode)){
            statusString = "passed";
        } else if(this.errorCodes.contains(exitCode)){
            statusString = "error";
        }

        JSONObject newStatus = new JSONObject();
        newStatus.put("complianceID", complianceID);
        newStatus.put("scriptName", scriptName);
        newStatus.put("statusCode", exitCode);
        newStatus.put("status", statusString);
        newStatus.put("checked", true);
        newStatus.put("lastCheck", new Date());
        this.status = newStatus;
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(this.cronTime);
    }

    public String getComplianceID() {
        return complianceID;
    }

    public JSONObject getStatus(){
        return this.status;
    }

    void register(){
        this.stemApp.getCallBackService().registerCallbackListener(this, this.stemPlugin);
    }
    void unregister(){
        this.stemApp.getCallBackService().unregisterCallbackListener(this);
    }
}
