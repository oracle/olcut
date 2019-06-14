package com.oracle.labs.mlrg.olcut.config;

/**
 * A simple property is a single String which can be parsed as a field value (either by conversion or lookup in
 * a {@link ConfigurationManager}).
 */
public final class SimpleProperty implements Property {

    private final String value;

    public SimpleProperty(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public SimpleProperty copy() {
        return new SimpleProperty(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleProperty)) return false;
        SimpleProperty that = (SimpleProperty) o;
        return getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

}
