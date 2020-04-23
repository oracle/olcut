/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.oracle.labs.mlrg.olcut.command;

import java.util.Map;

/**
 * A layer over a command interpreter that will collect commands that should be
 * added to an underlying command interpreter.
 */
public class LayeredCommandInterpreter extends CommandInterpreter {

    private final String layerTag;
    
    private final String layerName;
    
    public LayeredCommandInterpreter(String layerTag, String layerName) {
        super(false);
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
