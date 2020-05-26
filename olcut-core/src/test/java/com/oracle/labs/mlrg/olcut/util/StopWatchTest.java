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
