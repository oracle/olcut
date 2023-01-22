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

package com.oracle.labs.mlrg.olcut.config.edn;

import us.bpsm.edn.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClassnameMapper {

    private final Map<String, String> prefixes;

    public ClassnameMapper(Map<String, String> prefixes) {
        this.prefixes = prefixes;
    }

    public ClassnameMapper() {
        prefixes = new HashMap<>();
        prefixes.put("irml", "com.oracle.labs.irml");
        prefixes.put("mlrg", "com.oracle.labs.mlrg");
    }

    public List<Symbol> write(String cl) {
        List<Symbol> res = new ArrayList<>();
        String clm = cl;
        for(Map.Entry<String, String> e: prefixes.entrySet()) {
            if(cl.startsWith(e.getValue())) {
                res.add(Symbol.newSymbol(e.getKey()));
                clm = cl.substring(e.getValue().length()+1);
                break;
            }
        }
        for(String s: clm.split("\\.")) {
            res.add(Symbol.newSymbol(s));
        }
        return res;
    }

    public String read(List<Symbol> cl) {
        StringBuilder sb = new StringBuilder();
        Iterator<Symbol> iter = cl.iterator();
        String s = iter.next().toString();
        sb.append(prefixes.getOrDefault(s, s));
        while(iter.hasNext()) {
            sb.append(".").append(iter.next().toString());
        }
        return sb.toString();
    }
}
