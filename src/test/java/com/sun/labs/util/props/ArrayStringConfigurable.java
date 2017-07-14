package com.sun.labs.util.props;

/**
 *
 */
public class ArrayStringConfigurable implements Configurable {

    @Config
    private StringConfigurable[] stringArray;

    public StringConfigurable[] getArray() {
        return stringArray;
    }

}
