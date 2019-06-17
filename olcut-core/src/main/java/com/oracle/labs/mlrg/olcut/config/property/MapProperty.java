package com.oracle.labs.mlrg.olcut.config.property;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A MapProperty is a container for a Map from String to Property.
 */
public final class MapProperty implements Property {

    private final Map<String,SimpleProperty> map;

    public MapProperty(Map<String,SimpleProperty> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    public Map<String,SimpleProperty> getMap() {
        return map;
    }

    public MapProperty copy() {
        Map<String,SimpleProperty> output = new HashMap<>();

        for (Map.Entry<String,SimpleProperty> e : map.entrySet()) {
            output.put(e.getKey(),e.getValue().copy());
        }

        return new MapProperty(output);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapProperty)) return false;
        MapProperty that = (MapProperty) o;
        return getMap().equals(that.getMap());
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public static MapProperty createFromStringMap(Map<String,String> input) {
        Map<String, SimpleProperty> output = new HashMap<>();

        for (Map.Entry<String,String> e : input.entrySet()) {
            output.put(e.getKey(),new SimpleProperty(e.getValue()));
        }

        return new MapProperty(output);
    }
}
