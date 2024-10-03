package de.stem.stemSystem.modules.scriptModule.exceptions;

public class InvalidScriptException extends ScriptException {
    public InvalidScriptException() {
        super("This script is not a valid stem script!");
    }
}
