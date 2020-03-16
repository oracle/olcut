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

import java.util.Deque;
import java.util.List;
import java.util.Map;

import jline.console.completer.Completer;

/**
 * A <a href="http://jline.sourceforge.net">JLine</a>-style Completor that will
 * complete partial text based on all commands currently defined in the
 * {@link CommandInterpreter}
 */
public class CommandCompleter implements Completer {

    Map<String,CommandInterface> cmdMap;
    
    Deque<LayeredCommandInterpreter> interpreters;
    /**
     * Create a CommandCompletor given the map that maps command names to their
     * command objects. The live map of commands must be provided if this
     * completor is to reflect commands added after instantiation.
     * @param cmdMap The commands understood by this completer.
     * @param interpreters The interpreters used.
     */
    public CommandCompleter(Map<String,CommandInterface> cmdMap, Deque<LayeredCommandInterpreter> interpreters) {
        this.cmdMap = cmdMap;
        this.interpreters = interpreters;
    }

    /**
     * See <a href="http://jline.sourceforge.net/apidocs/jline/Completor.html">
     * Completor</a> in the JLine javadoc.
     * @param buff The input text buffer.
     * @param i The index in the buffer to look up to.
     * @param ret The list of possible completions.
     * @return -1 if there are no completions, 0 otherwise.
     */
    @Override
    public int complete(String buff, int i, List<CharSequence> ret) {
        String prefix = "";
        if (buff != null) {
            prefix = buff.substring(0, i);
        }
        for (String command : cmdMap.keySet()) {
            if (command.toLowerCase().startsWith(prefix.toLowerCase())) {
                ret.add(command);
            }
        }
        for(LayeredCommandInterpreter lci : interpreters) {
            for(String command : lci.commands.keySet()) {
                if (command.toLowerCase().startsWith(prefix.toLowerCase())) {
                    ret.add(command + "." + lci.getLayerTag());
                }
            }
        }
        if (ret.size() == 1) {
            ret.set(0, ret.get(0) + " ");
        }
        return (ret.isEmpty() ? -1 : 0);
    }
}
