package com.sun.labs.util.command;

import java.util.Map;

/**
 * A layer over a command interpreter that will collect commands that should be
 * added to an underlying command interpreter.
 */
public class LayeredCommandInterpreter extends CommandInterpreter {

    private String layerTag;
    
    public LayeredCommandInterpreter(String layerTag) {
        this.layerTag = layerTag;
    }

    public String getLayerTag() {
        return layerTag;
    }

    public Map<String, CommandInterface> getCommands() {
        return commands;
    }

    public Map<String, CommandGroup> getCommandGroups() {
        return commandGroups;
    }

    
    
}
