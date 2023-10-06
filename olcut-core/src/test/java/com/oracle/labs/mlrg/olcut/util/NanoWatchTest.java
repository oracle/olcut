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

public class NanoWatchTest {
    public static class TestNanoWatch extends NanoWatch {
        public void setTime(long time) {
            this.time = time;
        }
    }

    @Test
    public void toStringTest() {
        TestNanoWatch nw = new TestNanoWatch();
        nw.setTime(987654321);
        assertEquals("987,654,321 nanos", nw.toString());

        nw.setTime(TimeUnit.MINUTES.toNanos(42) + 321);
        assertEquals("00:42:00 and 321 nanos", nw.toString());

        nw.setTime(TimeUnit.DAYS.toNanos(5) + 654321);
        assertEquals("5 days 00:00:00 and 654,321 nanos", nw.toString());
    }

    @Test
    public void conversionTest() {
        TestNanoWatch nw = new TestNanoWatch();
        //
        // Set 10 millis worth of nanos
        nw.setTime(TimeUnit.MILLISECONDS.toNanos(10));

        assertEquals(10, nw.getTimeMillis());
    }

    @Test
    public void accumulateTest() {
        StopWatchTest.TestStopWatch sw = new StopWatchTest.TestStopWatch();
        //
        // Set 5 seconds on the milli timer
        sw.setTime(TimeUnit.SECONDS.toMillis(5));

        //
        // Add it to a nano time
        TestNanoWatch nw = new TestNanoWatch();
        nw.accumulate(sw);

        //
        // We should have 5 seconds on the nanowatch, not anything else
        assertEquals(5, TimeUnit.NANOSECONDS.toSeconds(nw.getTimeNanos()));
    }
}
