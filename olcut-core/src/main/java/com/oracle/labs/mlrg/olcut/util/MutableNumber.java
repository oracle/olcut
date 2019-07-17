package com.oracle.labs.mlrg.olcut.util;

import java.util.HashMap;
import java.util.Map;

public abstract class MutableNumber extends Number {

    public abstract MutableNumber copy();

    public static <T extends MutableNumber> Map<String,T> copyMap(Map<String,T> input) {
        HashMap<String, T> output = new HashMap<>();

        for (Map.Entry<String, T> e : input.entrySet()) {
            @SuppressWarnings("unchecked") //copy returns the same type.
            T copy = (T) e.getValue().copy();
            output.put(e.getKey(), copy);
        }

        return output;
    }

}
