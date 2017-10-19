package com.oracle.labs.mlrg.olcut.config;

/**
 *
 */
public class ArrayStringConfigurable implements Configurable {

    @ConfigurableName
    private String name;

    @Config
    private StringConfigurable[] stringArray;

    public String getName() {
        return name;
    }

    public StringConfigurable[] getArray() {
        return stringArray;
    }

}
