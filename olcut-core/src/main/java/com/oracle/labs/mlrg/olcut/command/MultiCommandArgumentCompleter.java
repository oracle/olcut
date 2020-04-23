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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    protected void updateCompleters() {
        logger.log(Level.FINER, "Updating all completers starting at base layer");
        updateCompleters(null, cmdMap);
        for(LayeredCommandInterpreter lci : interpreters) {
            logger.log(Level.FINER, "Updating compls for " + lci.getLayerName());
            updateCompleters(lci.getLayerTag(), lci.getCommands());
        }
    }
    
    protected void updateCompleters(String layerTag, Map<String, CommandInterface> commands) {
        for (Map.Entry<String,CommandInterface> command : commands.entrySet()) {
            String lCommand = command.getKey();
            if(layerTag != null) {
                lCommand = lCommand + "." + layerTag;
            }
            logger.log(Level.FINER, "Checking for command " + lCommand);
            if (!compMap.containsKey(lCommand)) {
                CommandInterface ci = command.getValue();
                logger.log(Level.FINER, "compMap does not yet contain " + lCommand);
                final Completer[] compToAdd;
                if (ci instanceof CompleterCommandInterface) {
                    logger.log(Level.FINER, "Adding custom completer for " + lCommand);
                    CompleterCommandInterface cci
                            = (CompleterCommandInterface) ci;
                    compToAdd = cci.getCompleters();
                } else {
                    compToAdd = null;
                    logger.log(Level.FINER, "Adding with no completer for " + lCommand);
                }
                compMap.put(lCommand, compToAdd);
                //
                // Check to see if this command was ambiguous or not. If not, add a simple
                // version of it as well. Skip this if this command is non-qualified or has no completer. We
                // don't need to bother with no-completer since this is a map for finding completers.
                if (!lCommand.equals(command.getKey()) && compToAdd != null) {
                    if (!compMap.containsKey(command.getKey())) {
                        //
                        // Add an entry for the base command without the layer
                        logger.log(Level.FINER, "Adding non-qualified name for unambiguous command " + lCommand);
                        compMap.put(command.getKey(), compToAdd);
                    } else {
                        //
                        // We already have a non-qualified command with that name. See if the non-qualified
                        // command name also has a qualified name that refers to the same completers. If so,
                        // remove the non-qualified name as this is an ambiguous entry.
                        Completer[] nonQ = compMap.get(command.getKey());
                        List<String> qualifieds = compMap.keySet().stream().
                                filter(c -> c.startsWith(command.getKey() + ".")).collect(Collectors.toList());
                        //
                        // If anybody other than myself is in there, we're ambiguous
                        if (qualifieds.stream().filter(c -> compMap.get(c) == compToAdd).count() > 1) {
                            logger.log(Level.FINER, "Removing ambiguous non-qualified command " + command.getKey());
                            compMap.remove(command.getKey());
                        }

                    }
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
        // Update our subcommand completers in case there are new ones
        updateCompleters();
        if (line.wordIndex() == 0) {
            comp = commandCompleter;
        } else {
            //
            // Get out the list of completers we should use based on the current
            // subcommand
            String wholeBuff = line.line();
            Completer[] completers = compMap.get(wholeBuff.substring(0, wholeBuff.indexOf(" ")));
            
            if (completers == null) {
                //
                // No completions at this point
                logger.log(Level.FINE, "No completers found for #" + wholeBuff.substring(0, wholeBuff.indexOf(" ")) + "#");
                return;
            }

            // if we are beyond the end of the completers, just use the last one
            if (line.wordIndex() >= completers.length) {
                comp = completers[completers.length - 1];
            } else {
                comp = completers[line.wordIndex()-1];
            }
        }
        logger.log(Level.FINE,"evaluating " + line.word() + " with " + comp.getClass().getSimpleName() + " at " + line.wordCursor());

        comp.complete(reader, line, candidates);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Completing " + line.line() + "(pos=" + line.wordCursor() + ") "
                    + "with: " + candidates.stream().map(s -> s.value()).collect(Collectors.joining(", ", "[", "]")));
        }

    }
}
