package de.stem.stemSystem.modules.scriptModule;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import de.stem.stemSystem.modules.scriptModule.exceptions.InvalidScriptException;
import de.stem.stemSystem.modules.scriptModule.exceptions.ScriptNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptManager extends AbstractModule {

    private final STEMSystemApp stemSystemApp;
    public static File scriptDirectory = new File("scripts");
    private FileConfiguration fileConfiguration;
    ArrayList<StemScript> stemScripts;

    public ScriptManager(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
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
        File file = new File(scriptDirectory, name + ".stemsh");
        if (!file.exists()) {
            throw new ScriptNotFoundException();
        }
        List<String> requiredParameters = this.checkValidScript(file);
        return new StemScript(this, file, requiredParameters);
    }

    private List<String> checkValidScript(File file) {
        List<String> requiredParameters = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            if (!reader.readLine().equalsIgnoreCase("#[STEM-SCRIPT]")) {
                throw new InvalidScriptException();
            }
            String inputParameters = reader.readLine();

            if (!inputParameters.startsWith("#[") || !inputParameters.endsWith("]")) {
                throw new InvalidScriptException();
            }
            String cleanedInput = inputParameters.substring(2, inputParameters.length() - 1);
            if (!cleanedInput.isEmpty()) {
                requiredParameters.addAll(Arrays.asList(cleanedInput.split(", ")));
            }
        } catch (IOException ignored) {
        }
        return requiredParameters;
    }
}
