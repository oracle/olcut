package com.sun.labs.util.props;

/**
 *
 */
public class ArrayConfigurable implements Configurable {

    @Config
    int[] intArray;
    @Config
    long[] longArray;

    @Config
    float[] floatArray;
    @Config
    double[] doubleArray;

}
