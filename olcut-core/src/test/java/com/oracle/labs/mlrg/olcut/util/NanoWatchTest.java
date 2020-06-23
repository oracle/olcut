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
