package com.oracle.labs.mlrg.olcut.util;

import java.util.HashMap;
import java.util.Map;

public abstract class MutableNumber extends Number {

    public abstract MutableNumber copy();

    public static <T,U extends MutableNumber> Map<T,U> copyMap(Map<T,U> input) {
        HashMap<T,U> output = new HashMap<>();

        for (Map.Entry<T,U> e : input.entrySet()) {
            @SuppressWarnings("unchecked") //copy returns the same type.
            U copy = (U) e.getValue().copy();
            output.put(e.getKey(), copy);
        }

        return output;
    }

}
