/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.oracle.labs.mlrg.olcut.util;

/**
 * A mutable version of java.lang.Double.
 */
public class MutableDouble extends MutableNumber {
    private static final long serialVersionUID = 1L;

    private double value;

    public MutableDouble(double value) {
        this.value = value;
    }

    public MutableDouble() {
        value = 0L;
    }

    public MutableDouble(MutableDouble other) {
        value = other.value;
    }

    public MutableDouble(Number other) {
        value = other.doubleValue();
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

    @Override
    public MutableDouble copy() {
        return new MutableDouble(value);
    }
}
