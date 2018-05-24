package com.oracle.labs.mlrg.olcut.config;

/**
 *
 */
public class ArrayConfigurable implements Configurable {

    @Config
    public byte[] byteArray;
    @Config
    public short[] shortArray;
    @Config
    public int[] intArray;
    @Config
    public long[] longArray;

    @Config
    public float[] floatArray;
    @Config
    public double[] doubleArray;

    @Config
    public char[] charArray;

}
