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

package de.linzn.stem.modules.scriptModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.scriptModule.exceptions.InvalidScriptException;
import de.linzn.stem.modules.scriptModule.exceptions.ScriptNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptManager extends AbstractModule {

    private final STEMApp stemApp;
    public static File scriptDirectory = new File("scripts");
    private FileConfiguration fileConfiguration;
    ArrayList<StemScript> stemScripts;

    public ScriptManager(STEMApp stemApp) {
        this.stemApp = stemApp;
        this.stemScripts = new ArrayList<>();
        this.init();
        this.initConfig();
    }

    private void init() {
        if (!scriptDirectory.exists()) {
            scriptDirectory.mkdir();
        }
    }

    private void initConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(new File("module_scripts.yml"));
        this.fileConfiguration.save();
    }

    @Override
    public void onShutdown() {
        for (StemScript stemScript : this.stemScripts) {
            stemScript.destroy();
        }
        this.stemScripts.clear();
    }

    public StemScript getStemScript(String name) throws ScriptNotFoundException, InvalidScriptException {
        return getStemScript(scriptDirectory, name);
    }

    public StemScript getStemScript(String subDirectoryName, String name) throws InvalidScriptException, ScriptNotFoundException {
        File subDirectory = new File(scriptDirectory, subDirectoryName);
        return getStemScript(subDirectory, name);
    }

    public StemScript getStemScript(File directory, String name) throws ScriptNotFoundException, InvalidScriptException {
        if(!directory.exists() || !directory.isDirectory()){
            throw new ScriptNotFoundException();
        }

        File file = new File(directory, name + ".stemsh");
        if (!file.exists()) {
            throw new ScriptNotFoundException();
        }
        List<String> requiredParameters = this.checkValidScript(file);
        return new StemScript(this, file, requiredParameters);
    }

    private List<String> checkValidScript(File file) throws InvalidScriptException {
        List<String> requiredParameters = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            if (!reader.readLine().equalsIgnoreCase("#[STEM-SCRIPT]")) {
                throw new InvalidScriptException();
            }
            String inputParameters = reader.readLine();

            if (inputParameters.startsWith("#[") && inputParameters.endsWith("]")) {
                String cleanedInput = inputParameters.substring(2, inputParameters.length() - 1);
                if (!cleanedInput.isEmpty()) {
                    requiredParameters.addAll(Arrays.asList(cleanedInput.split(", ")));
                }
            } else {
                throw new InvalidScriptException();
            }
        } catch (IOException ignored) {
        }
        return requiredParameters;
    }
}
