package com.oracle.labs.mlrg.olcut.command;

import java.util.Map;

/**
 * A layer over a command interpreter that will collect commands that should be
 * added to an underlying command interpreter.
 */
public class LayeredCommandInterpreter extends CommandInterpreter {

    private String layerTag;
    
    private String layerName;
    
    public LayeredCommandInterpreter(String layerTag, String layerName) {
        this.layerTag = layerTag;
        this.layerName = layerName;
    }
    
    @Override
    protected void dumpCommands() {
        int count = 0;
        for(CommandGroupInternal cg : commandGroups.values()) {
            if(cg.getGroupName().equals(STANDARD_COMMANDS_GROUP_NAME)) {
                continue;
            }
            count = dumpGroup(cg, count);
        }
        for(LayeredCommandInterpreter lci : interpreters) {
            putResponse(String.format("Commands labeled %s", lci.getLayerTag()));
            lci.dumpCommands();
        }
    }


    public String getLayerTag() {
        return layerTag;
    }

    public String getLayerName() {
        return layerName;
    }
    
    public Map<String, CommandInterface> getCommands() {
        return commands;
    }

    public Map<String, CommandGroupInternal> getCommandGroups() {
        return commandGroups;
    }

    @Override
    public String toString() {
        return "LayeredCommandInterpreter name: " + layerName + ", tag: " + layerTag;
    }
    
}
