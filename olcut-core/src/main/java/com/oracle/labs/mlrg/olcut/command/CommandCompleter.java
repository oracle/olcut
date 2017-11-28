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
     */
    public CommandCompleter(Map<String,CommandInterface> cmdMap, Deque<LayeredCommandInterpreter> interpreters) {
        this.cmdMap = cmdMap;
        this.interpreters = interpreters;
    }

    /**
     * See <a href="http://jline.sourceforge.net/apidocs/jline/Completor.html">
     * Completor</a> in the JLine javadoc.
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
