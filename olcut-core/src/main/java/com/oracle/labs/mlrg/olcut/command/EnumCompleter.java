
package com.oracle.labs.mlrg.olcut.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jline.console.completer.Completer;

/**
 * A JLine-style command line argument completor that will complete the current
 * word with one of the values from an enum type.  This completor is case
 * insensitive to the input string, so where enum values are typically all
 * capital, you may start typing in lower case.  The completed value will
 * match the case of the actual enum value so that the value may be parsed
 * correctly.
 */
public class EnumCompleter <E extends Enum<E>> implements Completer {
        
    protected Set<String> vals = new HashSet<String>();
        
    public EnumCompleter(Class<E> enumType) {
        E[] consts = enumType.getEnumConstants();
        for (int i = 0; i < consts.length; i++) {
            vals.add(consts[i].name());
        }
    }
        
    @Override
    public int complete(String buff, int i, List<CharSequence> ret) {
        String prefix = "";
        if (buff != null) {
            prefix = buff.substring(0, i);
        }
        for (String val : vals) {
            if (val.toLowerCase().startsWith(prefix.toLowerCase())) {
                ret.add(val);
            }
        }
        if (ret.size() == 1) {
            ret.set(0, ret.get(0) + " ");
        }
        return (ret.isEmpty() ? -1 : 0);
    }
}
