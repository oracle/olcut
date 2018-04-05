package com.oracle.labs.mlrg.olcut.util;

/**
 * A mutable version of java.lang.Double.
 */
public class MutableDouble extends Number {
    private static final long serialVersionUID = 1L;

    private double value;

    public MutableDouble(double value) {
        this.value = value;
    }

    public MutableDouble() {
        value = 0L;
    }

    public void increment(double other) {
        value += other;
    }

    public void increment(MutableDouble other) {
        value += other.value;
    }

    public void increment() {
        value++;
    }

    public void decrement(double other) {
        value -= other;
    }

    public void decrement(MutableDouble other) {
        value -= other.value;
    }

    public void decrement() {
        value--;
    }

    public void set(double other) {
        value = other;
    }

    public void multiply(double other) {
        value *= other;
    }

    public void multiply(MutableDouble other) {
        value *= other.value;
    }

    public void divide(double other) {
        value /= other;
    }

    public void divide(MutableDouble other) {
        value /= other.value;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public float floatValue() {
        return (float) value;
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
