
package com.sun.labs.util.command;

import java.util.List;
import java.util.Map;
import jline.Completor;

/**
 * A <a href="http://jline.sourceforge.net">JLine</a>-style Completor that
 * will complete partial text based on all commands currently defined in
 * the {@link CommandInterpreter}
 */
public class CommandCompletor implements Completor {
    protected Map<String,CommandInterface> cmdMap;
    
    /**
     * Create a CommandCompletor given the map that maps command names to their
     * command objects.  The live map of commands must be provided if this
     * completor is to reflect commands added after instantiation.
     * @param commands 
     */
    public CommandCompletor(Map<String,CommandInterface> commands) {
        cmdMap = commands;
    }
    
    /**
     * See <a href="http://jline.sourceforge.net/apidocs/jline/Completor.html">
     * Completor</a> in the JLine javadoc.
     */
    @Override
    public int complete(String buff, int i, List ret) {
        String prefix = buff.substring(0, i);
        for (String cmd : cmdMap.keySet()) {
            if (cmd.toLowerCase().startsWith(prefix.toLowerCase())) {
                ret.add(cmd);
            }
        }
        if (ret.size() == 1) {
            ret.set(0, ret.get(0) + " ");
        }
        return (ret.isEmpty() ? -1 : 0);
    }
}
