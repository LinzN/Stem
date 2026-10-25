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

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.AbstractModule;
import de.linzn.stem.modules.scriptModule.exceptions.InvalidScriptException;
import de.linzn.stem.modules.scriptModule.exceptions.ScriptNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptManager extends AbstractModule {

    public static File scriptDirectory = new File("scripts");
    final Set<StemScript> stemScripts = ConcurrentHashMap.newKeySet();
    private FileConfiguration fileConfiguration;

    public ScriptManager(STEMApp stemApp) {
        super(stemApp);
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
        for (StemScript stemScript : List.copyOf(this.stemScripts)) {
            stemScript.destroy();
        }
        this.stemScripts.clear();
    }

    public StemScript getStemScript(String name) throws ScriptNotFoundException, InvalidScriptException {
        return getStemScript(scriptDirectory, name);
    }

    public StemScript getStemScript(String subDirectoryName, String name) throws InvalidScriptException, ScriptNotFoundException {
        File subDirectory = new File(scriptDirectory, subDirectoryName);
        if (!isInside(scriptDirectory, subDirectory)) {
            throw new ScriptNotFoundException();
        }
        return getStemScript(subDirectory, name);
    }

    public StemScript getStemScript(File directory, String name) throws ScriptNotFoundException, InvalidScriptException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new ScriptNotFoundException();
        }

        if (name == null || name.isBlank()) {
            throw new ScriptNotFoundException();
        }
        File file = new File(directory, name + ".stemsh");
        /* Block names like "../../tmp/evil" that point outside the script directory */
        if (!isInside(directory, file) || !file.isFile()) {
            throw new ScriptNotFoundException();
        }
        List<String> requiredParameters = this.checkValidScript(file);
        return new StemScript(this, file, requiredParameters);
    }

    private static boolean isInside(File directory, File file) {
        Path base = directory.toPath().toAbsolutePath().normalize();
        Path target = file.toPath().toAbsolutePath().normalize();
        return target.startsWith(base);
    }

    private List<String> checkValidScript(File file) throws InvalidScriptException {
        List<String> requiredParameters = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            reader.readLine();
            String marker = reader.readLine();
            if (marker == null || !marker.trim().equalsIgnoreCase("#[STEM-SCRIPT]")) {
                throw new InvalidScriptException();
            }
            String inputParameters = reader.readLine();
            if (inputParameters == null) {
                throw new InvalidScriptException();
            }
            inputParameters = inputParameters.trim();
            if (inputParameters.startsWith("#[") && inputParameters.endsWith("]")) {
                String cleanedInput = inputParameters.substring(2, inputParameters.length() - 1);
                if (!cleanedInput.isEmpty()) {
                    requiredParameters.addAll(Arrays.asList(cleanedInput.split(", ")));
                }
            } else {
                throw new InvalidScriptException();
            }
        } catch (IOException e) {
            throw new InvalidScriptException();
        }
        return requiredParameters;
    }
}
