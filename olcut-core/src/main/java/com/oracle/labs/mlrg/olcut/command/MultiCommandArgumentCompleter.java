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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

/**
 * Mostly re-implements the ArgumentCompletor from jline, but allows for
 * command-specific argument completors. This is used internally by the
 * command interpreter to allow each CommandInterface to provide a list
 * of Completors to use for its arguments.  It should not be used directly
 * by anything else.
 */
class MultiCommandArgumentCompleter implements Completer {

    private static final Logger logger = Logger.getLogger(MultiCommandArgumentCompleter.class.getName());

    protected final Map<String,CommandInterface> cmdMap;
    
    protected final Deque<LayeredCommandInterpreter> interpreters;
    
    protected final Map<String, Completer[]> compMap;

    protected final CommandCompleter commandCompleter;

    /**
     * Creates completors for all the commands in cmdMap that implement
     * CompletorProvider.
     * 
     * @param cmdMap a reference to the shell's internal command map, reused
     *               to check for added commands
     */
    public MultiCommandArgumentCompleter(Map<String,CommandInterface> cmdMap,
                                         Deque<LayeredCommandInterpreter> interpreters) {
        this.cmdMap = cmdMap;
        this.interpreters = interpreters;
        this.commandCompleter = new CommandCompleter(cmdMap, interpreters);
        this.compMap = new HashMap<>();
    }
    
    /**
     * Scan through the commands in the cmdMap and add completors for any
     * command we don't already have a completor for.
     */
    protected void updateCompletors() {
        updateCompletors(null, cmdMap);
        for(LayeredCommandInterpreter lci : interpreters) {
            updateCompletors(lci.getLayerTag(), lci.getCommands());
        }
    }
    
    protected void updateCompletors(String layerTag, Map<String, CommandInterface> commands) {
        for (Map.Entry<String,CommandInterface> command : commands.entrySet()) {
            String lCommand = command.getKey();
            if(layerTag != null) {
                lCommand = command + "." + layerTag;
            }
            if (!compMap.containsKey(lCommand)) {
                CommandInterface ci = command.getValue();
                if (ci instanceof CompleterCommandInterface) {
                    CompleterCommandInterface cci
                            = (CompleterCommandInterface) ci;
                    compMap.put(lCommand, cci.getCompleters());
                } else {
                    compMap.put(lCommand, null);
                }
            }
        }
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, final List<Candidate> candidates) {
        logger.log(Level.FINE,"\ncomplete invoked with " + line.line());
        logger.log(Level.FINE, "Line(wordCursor='"+line.wordCursor()+"',word='"+line.word()+"',wordIndex='"+line.wordIndex()+"',cursor='"+line.cursor()+"'");
        Objects.requireNonNull(line);
        Objects.requireNonNull(candidates);

        if (line.wordIndex() < 0) {
            return;
        }

        final Completer comp;

        //
        // Update our subcommand completors in case there are new ones
        updateCompletors();
        if (line.wordIndex() == 0) {
            comp = commandCompleter;
        } else {
            //
            // Get out the list of completors we should use based on the current
            // subcommand
            String wholeBuff = line.line();
            Completer[] completors = compMap.get(wholeBuff.substring(0, wholeBuff.indexOf(" ")));
            
            if (completors == null) {
                //
                // No completions at this point
                return;
            }

            // if we are beyond the end of the completors, just use the last one
            if (line.wordIndex() >= completors.length) {
                comp = completors[completors.length - 1];
            } else {
                comp = completors[line.wordIndex()-1];
            }
        }
        logger.log(Level.FINE,"evaluating " + line.word() + " with " + comp.getClass().getSimpleName() + " at " + line.wordCursor());

        comp.complete(reader, line, candidates);

        logger.log(Level.FINE,"Completing " + line.line() + "(pos=" + line.wordCursor() + ") "
            + "with: " + candidates);
    }
}
