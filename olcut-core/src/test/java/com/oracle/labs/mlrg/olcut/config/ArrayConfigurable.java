package com.oracle.labs.mlrg.olcut.config;

/**
 *
 */
public class ArrayConfigurable implements Configurable {

    @Config
    byte[] byteArray;
    @Config
    short[] shortArray;
    @Config
    int[] intArray;
    @Config
    long[] longArray;

    @Config
    float[] floatArray;
    @Config
    double[] doubleArray;

    @Config
    char[] charArray;

}
