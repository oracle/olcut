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

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for time specifications.
 */
public class TimeSpecTest {

    public TimeSpecTest() {
    }

    @Test
    public void testSingle() {
        long x = TimeSpec.parse("1s");
        assertEquals(x, TimeUnit.SECONDS.toMillis(1));
        x = TimeSpec.parse("10s");
        assertEquals(x, TimeUnit.SECONDS.toMillis(10));
        x = TimeSpec.parse("1m");
        assertEquals(x, TimeUnit.MINUTES.toMillis(1));
        x = TimeSpec.parse("1h");
        assertEquals(x, TimeUnit.HOURS.toMillis(1));
    }

    @Test
    public void testTwo() {
        long x = TimeSpec.parse("1m1s");
        assertEquals(TimeUnit.MINUTES.toMillis(1) + TimeUnit.SECONDS.toMillis(1), x);
        x = TimeSpec.parse("1h1m");
        assertEquals(TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(1), x);
        x = TimeSpec.parse("1d1h");
        assertEquals(TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(1), x);
    }

    @Test
    public void testThree() {
        long x = TimeSpec.parse("2d5h10m");
        assertEquals(TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(10), x);
    }

    @Test
    public void badOrder() {
        assertThrows(IllegalArgumentException.class, () -> {
            long x = TimeSpec.parse("2h5d");
        });
    }

    @Test
    public void badUnit() {
        assertThrows(IllegalArgumentException.class, () -> {
            long x = TimeSpec.parse("2h5ns");
        });
    }
}