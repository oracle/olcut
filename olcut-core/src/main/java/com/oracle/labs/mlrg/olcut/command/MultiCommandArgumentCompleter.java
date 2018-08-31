package com.oracle.labs.mlrg.olcut.command;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.internal.Log;

/**
 * Mostly re-implements the ArgumentCompletor from jline, but allows for
 * command-specific argument completors. This is used internally by the
 * command interpreter to allow each CommandInterface to provide a list
 * of Completors to use for its arguments.  It should not be used directly
 * by anything else.
 */
class MultiCommandArgumentCompleter extends ArgumentCompleter {

    protected Map<String,CommandInterface> cmdMap;
    
    protected Deque<LayeredCommandInterpreter> interpreters;
    
    protected Map<String,Completer[]> compMap;
    
    /**
     * ArgumentDelimiter.delim isn't accessible from here, so we'll mask
     * "delim" in the superclass with this one that is useable.
     */
    protected WhitespaceArgumentDelimiter delim =
            new WhitespaceArgumentDelimiter();
    /**
     * We need the reader we're working through to get which command we're
     * working on
     */
    protected ConsoleReader reader;
    
    /**
     * Creates completors for all the commands in cmdMap that implement
     * CompletorProvider.
     * 
     * @param cmdMap a reference to the shell's internal command map, reused
     *               to check for added commands
     */
    public MultiCommandArgumentCompleter(ConsoleReader reader,
                                         Map<String,CommandInterface> cmdMap, 
                                         Deque<LayeredCommandInterpreter> interpreters) {
        super((Completer)null);
        this.reader = reader;
        this.cmdMap = cmdMap;
        this.interpreters = interpreters;
        compMap = new HashMap<String,Completer[]>();
        setStrict(false);
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
    public int complete(final String buffer, final int cursor,
                        final List<CharSequence> candidates) {
        Log.debug("\ncomplete invoked with " + buffer);
        ArgumentList list = delim.delimit(buffer, cursor);
        int argpos = list.getArgumentPosition();
        int argIndex = list.getCursorArgumentIndex();

        if (argIndex < 0) {
            return -1;
        }

        //
        // Adjust index since delimit leaves off the initial command name
        argIndex--;
        final Completer comp;

        //
        // Update our subcommand completors in case there are new ones
        updateCompletors();
        Completer[] completors;
        if (list.getCursorArgumentIndex() == 0) {
            comp = new CommandCompleter(cmdMap, interpreters);
            completors = new Completer[] {comp};
        } else {
            //
            // Get out the list of completors we should use based on the current
            // subcommand
            String wholeBuff = reader.getCursorBuffer().buffer.toString();
            completors = compMap.get(wholeBuff.substring(0, wholeBuff.indexOf(" ")));
            
            if (completors == null) {
                //
                // No completions at this point
                return -1;
            }
            
            // if we are beyond the end of the completors, just use the last one
            if (argIndex >= completors.length) {
                comp = completors[completors.length - 1];
            } else {
                comp = completors[argIndex];
            }
        }
        Log.debug("evaluating " + list.getCursorArgument() + " with " + comp.getClass().getSimpleName() + " at " + argIndex);
        // ensure that all the previous completors are successful before
        // allowing this completor to pass (only if strict is true).
        for (int i = 0; isStrict() && (i < argIndex); i++) {
            Completer sub =
                completors[(i >= completors.length) ? (completors.length - 1) : i];
            String[] args = list.getArguments();
            String arg = ((args == null) || (i >= args.length)) ? "" : args[i];

            List<CharSequence> subCandidates = new LinkedList<>();

            if (sub.complete(arg, arg.length(), subCandidates) == -1) {
                return -1;
            }

            if (subCandidates.size() == 0) {
                return -1;
            }
        }

        int ret = comp.complete(list.getCursorArgument(), argpos, candidates);

        if (ret == -1) {
            return -1;
        }

        int pos = ret + (list.getBufferPosition() - argpos);

        /**
         *  Special case: when completing in the middle of a line, and the
         *  area under the cursor is a delimiter, then trim any delimiters
         *  from the candidates, since we do not need to have an extra
         *  delimiter.
         *
         *  E.g., if we have a completion for "foo", and we
         *  enter "f bar" into the buffer, and move to after the "f"
         *  and hit TAB, we want "foo bar" instead of "foo  bar".
         */
        if ((cursor != buffer.length()) && delim.isDelimiter(buffer, cursor)) {
            for (int i = 0; i < candidates.size(); i++) {
                String val = candidates.get(i).toString();

                while ((val.length() > 0)
                    && delim.isDelimiter(val, val.length() - 1)) {
                    val = val.substring(0, val.length() - 1);
                }

                candidates.set(i, val);
            }
        }

        Log.debug("Completing " + buffer + "(pos=" + cursor + ") "
            + "with: " + candidates + ": offset=" + pos);

        return pos;
    }
}
