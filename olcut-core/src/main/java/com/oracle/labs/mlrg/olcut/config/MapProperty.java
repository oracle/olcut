package com.oracle.labs.mlrg.olcut.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A MapProperty is a container for a Map from String to Property.
 */
public final class MapProperty implements Property {

    private final Map<String,Property> map;

    public MapProperty(Map<String,Property> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    public Map<String,Property> getMap() {
        return map;
    }

    public MapProperty copy() {
        Map<String,Property> output = new HashMap<>();

        for (Map.Entry<String,Property> e : map.entrySet()) {
            output.put(e.getKey(),e.getValue().copy());
        }

        return new MapProperty(output);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public static MapProperty createFromStringMap(Map<String,String> input) {
        Map<String, Property> output = new HashMap<>();

        for (Map.Entry<String,String> e : input.entrySet()) {
            output.put(e.getKey(),new SimpleProperty(e.getValue()));
        }

        return new MapProperty(output);
    }
}
