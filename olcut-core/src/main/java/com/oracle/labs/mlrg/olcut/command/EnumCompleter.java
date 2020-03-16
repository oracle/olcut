
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
