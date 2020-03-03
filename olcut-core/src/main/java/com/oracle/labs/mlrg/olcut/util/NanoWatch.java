
package com.oracle.labs.mlrg.olcut.util;

import java.io.Serializable;

/**
 * A nanosecond stop watch.
 */
public class NanoWatch extends StopWatch implements Serializable {

    /**
     * Starts the timer.
     */
    @Override
    public void start() {
        lastStart = System.nanoTime();
    }

    /**
     * Stops the timer.
     */
    @Override
    public void stop() {
        clicks++;
        lastTime = System.nanoTime() - lastStart;
        time += lastTime;
    }

    public long getTimeNanos() {
        return time;
    }

    public long getLastTimeNanos() {
        return lastTime;
    }

    /**
     * Gets the time for this nano stop watch in milliseconds.
     * @return the accumulated time for this stop watch in milliseconds.
     */
    public double getTimeMillis() {
        return getTime() / 1000000.0;
    }

    public double getLastTimeMillis() {
        return lastTime / 1000000.0;
    }

    public double getAvgTimeMillis() {
        return getTimeMillis() / (double) clicks;
    }
}
