/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.stem.modules.commandModule.defaultCommands;


import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.commandModule.ICommand;
import de.linzn.stem.modules.scriptModule.StemScript;
import de.linzn.stem.modules.scriptModule.exceptions.ScriptException;

public class RunScriptCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {

        if (args.length < 1 || (args.length - 1) % 2 != 0) {
            STEMApp.LOGGER.ERROR("Error: runscript [scriptname] <parameterame> <parametervalue> ...");
            return true;
        }

        String scriptName = args[0];

        try {
            StemScript stemScript = STEMApp.getInstance().getScriptManager().getStemScript(scriptName);
            if (args.length > 1) {
                for (int i = 1; i < args.length; i += 2) {
                    String paramName = args[i];
                    String paramValue = args[i + 1];
                    stemScript.addScriptParameter(paramName, paramValue);
                }
            }
            stemScript.start();
            STEMApp.LOGGER.CORE("Script execution started!");
            stemScript.await();
            int exitCode = stemScript.exitCode();
            STEMApp.LOGGER.CORE("Script executed with exit code: " + exitCode);
            if (exitCode != 0) {
                STEMApp.LOGGER.CORE("Errors: ");
                for (String line : stemScript.getOutput_lines()) {
                    STEMApp.LOGGER.WARNING(line);
                }
            }
        } catch (ScriptException e) {
            STEMApp.LOGGER.ERROR("Error while getting script file!");
            STEMApp.LOGGER.ERROR(e.getMessage());
        }

        return true;
    }

}
