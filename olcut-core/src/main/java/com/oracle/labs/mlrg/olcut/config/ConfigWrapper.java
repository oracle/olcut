package com.oracle.labs.mlrg.olcut.config;

/**
 * A wrapper for a Configurable that tests for equality. Used in the configuredComponents map.
 */
public class ConfigWrapper {

    public final Configurable config;

    public ConfigWrapper(Configurable config) {
        this.config = config;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ConfigWrapper) {
            return config == ((ConfigWrapper)other).config;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return config.hashCode();
    }

}
