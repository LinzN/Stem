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

package de.stem.stemSystem.modules.commandModule.defaultCommands;


import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.commandModule.ICommand;
import de.stem.stemSystem.modules.scriptModule.StemScript;
import de.stem.stemSystem.modules.scriptModule.exceptions.InvalidScriptException;
import de.stem.stemSystem.modules.scriptModule.exceptions.ScriptNotFoundException;

import java.io.IOException;
import java.util.ArrayList;

public class RunScriptCommand implements ICommand {

    @Override
    public boolean executeTerminal(String[] args) {
        if(args.length >=1) {
            String scriptName = args[0];
            try {
                StemScript stemScript = STEMSystemApp.getInstance().getScriptManager().getStemScript(scriptName);
                stemScript.start();
                STEMSystemApp.LOGGER.CORE("Script execution started!");
                stemScript.await();
                int exitCode = stemScript.exitCode();
                STEMSystemApp.LOGGER.CORE("Script executed with exit code: " + exitCode);
                if(exitCode != 0){
                    STEMSystemApp.LOGGER.CORE("Errors: ");
                    for (String line : stemScript.getOutput_lines()){
                        STEMSystemApp.LOGGER.WARNING(line);
                    }
                    for (String line : stemScript.getError_lines()){
                        STEMSystemApp.LOGGER.WARNING(line);
                    }
                }
            } catch (ScriptNotFoundException | InvalidScriptException e) {
                STEMSystemApp.LOGGER.ERROR("Error while getting script file!");
                STEMSystemApp.LOGGER.ERROR(e.getMessage());
            } catch (IOException e) {
                STEMSystemApp.LOGGER.ERROR("Error while starting script file!");
                STEMSystemApp.LOGGER.ERROR(e.getMessage());
            } catch (InterruptedException e) {
                STEMSystemApp.LOGGER.ERROR("Error while waiting of script end!");
                STEMSystemApp.LOGGER.ERROR(e.getMessage());
            }
        }
        return true;
    }

}
