package com.oracle.labs.mlrg.olcut.config;

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
