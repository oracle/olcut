package com.oracle.labs.mlrg.olcut.config.edn;

import us.bpsm.edn.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClassnameMapper {

    private Map<String, String> prefixes;

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
