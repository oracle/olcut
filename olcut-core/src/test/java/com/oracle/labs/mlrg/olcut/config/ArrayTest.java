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

package com.oracle.labs.mlrg.olcut.config;


import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * A set of tests for array types using Config
 */
public class ArrayTest {

    @Test
    public void arrayTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("arrayConfig.xml");
        ArrayConfigurable ac = (ArrayConfigurable) cm.lookup("a");
        assertArrayEquals(new int[]{1,2,3},ac.intArray, "int array not equal");
        assertArrayEquals(new long[]{9223372036854775807L,9223372036854775806L,5L},ac.longArray, "long array not equal");
        assertArrayEquals(new float[]{1.1f,2.3f,3.5f},ac.floatArray, 1e-6f, "float array not equal");
        assertArrayEquals(new double[]{1e-16,2e-16,3.16},ac.doubleArray,1e-16, "double array not equal");
    }

    @Test
    public void invalidArrayTest() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("arrayConfig.xml");
            ArrayConfigurable ac = (ArrayConfigurable) cm.lookup("invalid-char");
        }, "Invalid character array parsed, should have thrown PropertyException.");
    }

}
