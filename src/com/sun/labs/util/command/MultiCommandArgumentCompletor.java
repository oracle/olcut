/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.command;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;

/**
 * Mostly re-implements the ArgumentCompletor from jline, but allows for
 * command-specific argument completors. This is used internally by the
 * command interpreter to allow each CommandInterface to provide a list
 * of Completors to use for its arguments.  It should not be used directly
 * by anything else.
 */
class MultiCommandArgumentCompletor extends ArgumentCompletor {

    protected Map<String,CommandInterface> cmdMap;
    
    protected Map<String,Completor[]> compMap;
    
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
    public MultiCommandArgumentCompletor(ConsoleReader reader,
                                         Map<String,CommandInterface> cmdMap) {
        super((Completor)null);
        this.reader = reader;
        this.cmdMap = cmdMap;
        compMap = new HashMap<String,Completor[]>();
        setStrict(false);
    }
    
    /**
     * Scan through the commands in the cmdMap and add completors for any
     * command we don't already have a completor for.
     */
    protected void updateCompletors() {
        Set<String> commands = cmdMap.keySet();
        for (String command : commands) {
            if (!compMap.containsKey(command)) {
                CommandInterface ci = cmdMap.get(command);
                if (ci instanceof CompletorCommandInterface) {
                    CompletorCommandInterface cci =
                            (CompletorCommandInterface)ci;
                    compMap.put(command, cci.getCompletors());
                } else {
                    compMap.put(command, null);
                }
            }
        }
    }
    
    @Override
    public int complete(final String buffer, final int cursor,
                        final List candidates) {
        reader.debug("\ncomplete invoked with " + buffer);
        ArgumentList list = delim.delimit(buffer, cursor);
        int argpos = list.getArgumentPosition();
        int argIndex = list.getCursorArgumentIndex();

        if (argIndex < 0) {
            return -1;
        }

        //
        // Adjust index since delimit leaves off the initial command name
        argIndex--;
        final Completor comp;

        //
        // Update our subcommand completors in case there are new ones
        updateCompletors();
        Completor[] completors;
        if (list.getCursorArgumentIndex() == 0) {
            comp = new CommandCompletor(cmdMap);
            completors = new Completor[] {comp};
        } else {
            //
            // Get out the list of completors we should use based on the current
            // subcommand
            String wholeBuff = reader.getCursorBuffer().getBuffer().toString();
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
        reader.debug("evaluating " + list.getCursorArgument() + " with " + comp.getClass().getSimpleName() + " at " + argIndex);
        // ensure that all the previous completors are successful before
        // allowing this completor to pass (only if strict is true).
        for (int i = 0; getStrict() && (i < argIndex); i++) {
            Completor sub =
                completors[(i >= completors.length) ? (completors.length - 1) : i];
            String[] args = list.getArguments();
            String arg = ((args == null) || (i >= args.length)) ? "" : args[i];

            List subCandidates = new LinkedList();

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

        ConsoleReader.debug("Completing " + buffer + "(pos=" + cursor + ") "
            + "with: " + candidates + ": offset=" + pos);

        return pos;
    }
}
