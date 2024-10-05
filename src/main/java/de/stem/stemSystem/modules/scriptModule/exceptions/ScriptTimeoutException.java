package de.stem.stemSystem.modules.scriptModule.exceptions;

public class ScriptTimeoutException extends ScriptException {
    public ScriptTimeoutException() {
        super("Script runs into a timeout!");
    }
}
