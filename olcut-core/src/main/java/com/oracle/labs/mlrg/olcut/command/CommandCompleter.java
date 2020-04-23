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

import org.jline.reader.impl.completer.StringsCompleter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * A <a href="http://jline.sourceforge.net">JLine</a>-style Completor that will
 * complete partial text based on all commands currently defined in the
 * {@link CommandInterpreter}
 */
public class CommandCompleter extends StringsCompleter {

    /**
     * Create a CommandCompletor given the map that maps command names to their
     * command objects. The live map of commands must be provided if this
     * completor is to reflect commands added after instantiation.
     * @param cmdMap The commands understood by this completer.
     * @param interpreters The interpreters used.
     */
    public CommandCompleter(Map<String,CommandInterface> cmdMap, Deque<LayeredCommandInterpreter> interpreters) {
        super(createSupplier(cmdMap,interpreters));
    }

    private static Supplier<Collection<String>> createSupplier(Map<String,CommandInterface> cmdMap, Deque<LayeredCommandInterpreter> interpreters) {
        Objects.requireNonNull(cmdMap);
        Objects.requireNonNull(interpreters);
        return () -> {
            List<String> output = new ArrayList<>();
            // Load in commands
            output.addAll(cmdMap.keySet());
            //
            // Load in commands from layered interpreters, producing qualified names for each
            for (LayeredCommandInterpreter lci : interpreters) {
                output.addAll(lci.commands.keySet().stream().map(c -> c + "." + lci.getLayerTag()).collect(Collectors.toList()));
            }
            //
            // For each layered name, check if there would be name conflicts. If no conflicts exist, add the
            // non-qualified name as well. A conflict would be the same name command, or more than one command that has
            // the same basename and a ".<tag>" on it. (More than one because it will always match itself.)
            for (LayeredCommandInterpreter lci : interpreters) {
                for (String command : lci.commands.keySet()) {
                    if (!output.contains(command) &&
                            output.stream().filter(c -> c.startsWith(command + ".")).count() == 1) {
                        //
                        // Command and command.tag are not found, so go ahead and add it as a non-qualified command
                        output.add(command);
                        // Also remove the qualified name 'cause who needs it?
                        output.remove(command + "." + lci.getLayerTag());
                    }
                }
            }
            return output;
        };
    }
}
