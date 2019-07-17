package com.oracle.labs.mlrg.olcut.util;

/**
 * A mutable version of java.lang.Long.
 */
public class MutableLong extends MutableNumber {
    private static final long serialVersionUID = 1L;

    private long value;

    public MutableLong(long value) {
        this.value = value;
    }

    public MutableLong() {
        value = 0L;
    }

    public MutableLong(MutableLong other) {
        value = other.value;
    }

    public MutableLong(Number other) {
        value = other.longValue();
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

    public void decrement(long other) {
        value -= other;
    }

    public void decrement(MutableLong other) {
        value -= other.value;
    }

    public void decrement() {
        value--;
    }

    public void set(long other) {
        value = other;
    }

    public void multiply(long other) {
        value *= other;
    }

    public void multiply(MutableLong other) {
        value *= other.value;
    }

    public void divide(long other) {
        value /= other;
    }

    public void divide(MutableLong other) {
        value /= other.value;
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

    @Override
    public MutableLong copy() {
        return new MutableLong(value);
    }
}
