package com.sun.labs.util.props;

import java.util.Random;

/**
 * A configurable with two java.util.Random instances.
 */
public class RandomConfigurable implements Configurable {

    @Config
    public Random one;

    @Config
    public Random two;

    public RandomConfigurable() {
        one = new Random(1);
        two = new Random(2);
    }

}
