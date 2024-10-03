package de.stem.stemSystem.modules.scriptModule.exceptions;

public class ScriptNotStartedException extends ScriptException{
    public ScriptNotStartedException() {
        super("Script execution not started yet!");
    }
}
