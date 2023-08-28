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

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StopWatchTest {
    public static class TestStopWatch extends StopWatch {
        public void setTime(long time) {
            this.time = time;
        }
    }

    @Test
    public void toStringTest() {
        TestStopWatch sw = new TestStopWatch();
        //
        // Try a time in milliseconds
        sw.setTime(42);
        assertEquals("0.042s", sw.toString());
        //
        // Some time in seconds less than a minute
        sw.setTime((1 * 1000) + 42);
        assertEquals("1.042s", sw.toString());
        //
        // Some time in minutes less than an hour
        sw.setTime((37 * 60 * 1000) + (1 * 1000) + 42);
        assertEquals("00:37:01.042", sw.toString());
        //
        // Some time in hours less than a day
        sw.setTime((7 * 60 * 60 * 1000) + (37 * 60 * 1000) + (1 * 1000) + 42);
        assertEquals("07:37:01.042", sw.toString());
        //
        // Some time in days
        sw.setTime((12 * 24 * 60 * 60 * 1000) + (7 * 60 * 60 * 1000) + (37 * 60 * 1000) + (1 * 1000) + 42);
        assertEquals("12 days 7 hours 37 mins 1.042 seconds", sw.toString());
    }

    @Test
    public void accumulateNanosTest() throws Exception {
        TestStopWatch sw = new TestStopWatch();
        sw.setTime(5000);

        NanoWatchTest.TestNanoWatch nw = new NanoWatchTest.TestNanoWatch();
        nw.setTime(100); // 10 nanoseconds

        sw.accumulate(nw);
        assertEquals(5000, sw.getTimeMillis());

        nw.setTime(TimeUnit.MILLISECONDS.toNanos(1));
        sw.accumulate(nw);
        assertEquals(5001, sw.getTimeMillis());
    }
}
