package com.oracle.labs.mlrg.olcut.util;

/**
 * A mutable version of java.lang.Long.
 */
public class MutableLong extends Number {

    private long value;

    public MutableLong(long value) {
        this.value = value;
    }

    public MutableLong() {
        value = 0L;
    }

    public void increment(long other) {
        value += other;
    }

    public void increment(MutableLong other) {
        value += other.value;
    }

    public void increment() {
        value++;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public String toString() {
        return ""+value;
    }
}
