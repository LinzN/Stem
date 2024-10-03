package de.stem.stemSystem.modules.scriptModule.exceptions;

public class ScriptNotFoundException extends ScriptException {
    public ScriptNotFoundException() {
        super("Script not found with this name!");
    }
}
