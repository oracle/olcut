package com.sun.labs.util.props;

/**
 * Created by johsulli on 5/11/17.
 */
public class FooConfigurable implements Configurable {

    @Config
    public String name;

    @Config int value;

    public FooConfigurable(String name, int value) {
        this.name = name;
        this.value = value;
    }
}
