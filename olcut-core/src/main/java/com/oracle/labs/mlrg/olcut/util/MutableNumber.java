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

import java.util.HashMap;
import java.util.Map;

/**
 * The base class for the mutable primitive boxes in OLCUT.
 */
public abstract class MutableNumber extends Number {

    /**
     * Returns a copy of the MutableNumber.
     * @return A copy
     */
    public abstract MutableNumber copy();

    /**
     * Copies a map which contains mutable number values.
     * <p>
     * Used to duplicate maps which count things ensuring that they don't
     * double count values. The keys are *not* copied.
     * @param input The map to copy.
     * @param <T> The key type.
     * @param <U> The value type.
     * @return A copy of the map with copied values.
     */
    public static <T,U extends MutableNumber> Map<T,U> copyMap(Map<T,U> input) {
        HashMap<T,U> output = new HashMap<>();

        for (Map.Entry<T,U> e : input.entrySet()) {
            @SuppressWarnings("unchecked") //copy returns the same type.
            U copy = (U) e.getValue().copy();
            output.put(e.getKey(), copy);
        }

        return output;
    }

}
