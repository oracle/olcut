package com.sun.labs.util.props;

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
