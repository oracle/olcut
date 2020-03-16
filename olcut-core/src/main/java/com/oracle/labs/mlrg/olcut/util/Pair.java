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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * A pair of things.
 * Borrowed from Minion, modified hashcode when a pair of ints or longs.
 *
 * @param <T1> The type of the first object.
 * @param <T2> The type of the second object.
 */
public class Pair<T1, T2> implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(Pair.class.getName());
    
    private final T1 a;
    
    private final T2 b;

    public Pair(T1 a, T2 b) {
        this.a = a;
        this.b = b;
    }

    public T1 getA() {
        return a;
    }

    public T2 getB() {
        return b;
    }

    /**
     * Takes two arrays and zips them together into an array of Pairs.
     * @param <T1> The type contained in the first array.
     * @param <T2> The type contained in the second array.
     * @param first An array of values.
     * @param second Another array of values.
     * @return The zipped array.
     */
    public static <T1,T2> ArrayList<Pair<T1,T2>> zipArrays(ArrayList<T1> first, ArrayList<T2> second) {
        if (first.size() == second.size()) {
            ArrayList<Pair<T1,T2>> output = new ArrayList<>(first.size());

            for (int i = 0; i < first.size(); i++) {
                Pair<T1,T2> pair = new Pair<>(first.get(i),second.get(i));
                output.add(i, pair);
            }

            return output;
        } else {
            throw new IllegalArgumentException("Zipping requires arrays of the same length. first.size() = " + first.size() + ", second.size() = " + second.size());
        }
    }
    
    /**
     * Borrowed from java.util.SplittableRandom. Used to mix the integers in
     * hashcode.
     * Returns the 32 high bits of Stafford variant 4 mix64 function as int.
     */
    private static int mix32(long z) {
        z *= 0x62a9d9ed799705f5L;
        return (int)(((z ^ (z >>> 28)) * 0xcb24d0a5c88c35b3L) >>> 32);
    }
    
    /**
     * Overridden hashcode. Checks to see if the types are ints or longs, and
     * runs them through the mixing function from java.util.SplittableRandom if
     * they are. Then XORs the two hashcodes together.
     * @return A 32-bit integer.
     */
    @Override
    public int hashCode() {
        int aCode, bCode;
        
        if (a instanceof Integer) {
            aCode = mix32((Integer) a);
        } else if (a instanceof Long) {
            aCode = mix32((Long) a);
        } else {
            aCode = a.hashCode();
        }

        if (b instanceof Integer) {
            bCode = mix32((Integer) b);
        } else if (b instanceof Long) {
            bCode = mix32((Long) b);
        } else {
            bCode = b.hashCode();
        }
        
        return aCode ^ bCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!(obj instanceof Pair)) {
            return false;
        }
        final Pair<?,?> other = (Pair<?,?>) obj;
        if(this.a != other.a && (this.a == null || !this.a.equals(other.a))) {
            return false;
        }
        if(this.b != other.b && (this.b == null || !this.b.equals(other.b))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Pair{" + "a=" + a + ", b=" + b + '}';
    }
}
