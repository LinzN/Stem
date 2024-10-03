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

        if (!this.isValidScript(file)) {
            throw new InvalidScriptException();
        }

        return new StemScript(this, file);
    }

    private boolean isValidScript(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            if (reader.readLine().equalsIgnoreCase("#[STEM-SCRIPT]")) {
                return true;
            }
        } catch (IOException ignored) {
        }
        return false;
    }
}
