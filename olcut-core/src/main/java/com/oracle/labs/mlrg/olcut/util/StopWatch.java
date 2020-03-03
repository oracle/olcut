
package com.oracle.labs.mlrg.olcut.util;

import java.io.Serializable;

/**
 * A class implementing a simple stop watch that can be used for timing.
 */
public class StopWatch implements Serializable {

    /**
     * The amount of time accumulated on the timer.
     */
    protected long time;

    /**
     * The amount of time for the last start/stop pair.
     */
    protected long lastTime;

    /**
     * The number of starts and stops since the last reset.
     */
    protected long clicks;

    /**
     * The last start time.
     */
    protected long lastStart;

    /**
     * Creates a new stop watch.
     */
    public StopWatch() {
    }

    /**
     * Starts the timer.
     */
    public void start() {
        lastStart = System.currentTimeMillis();
    }

    /**
     * Stops the timer.
     */
    public void stop() {
        clicks++;
        lastTime = System.currentTimeMillis() - lastStart;
        time += lastTime;
    }

    /**
     * Resets the accumulated time.
     */
    public void reset() {
        time = 0;
        clicks = 0;
    }

    /**
     * Gets the number of milliseconds on the timer.
     * @return The number of milliseconds on the timer.
     */
    public long getTime() {
        return time;
    }

    public long getLastTime() {
        return lastTime;
    }

    public long getLastStart() {
        return lastStart;
    }

    public double getAvgTime() {
        return getTime() / (double) clicks;
    }

    /**
     * Gets the number of times that the watch was started and stopped since the last
     * reset.
     * @return The number of times that the watch was started and stopped since the last reset.
     * @see #reset()
     */
    public long getClicks() {
        return clicks;
    }

    public void accumulate(StopWatch sw) {
        if(sw == null) {
            return;
        }
        time += sw.time;
        clicks += sw.clicks;
    }

    public static String toTimeString(double millis) {
        if(millis < 1000) {
            return String.format("%.2fms", millis);
        }

        double secs = millis / 1000;

        if(secs < 60) {
            return String.format("%.2fs", secs);
        }

        double min = secs / 60;

        if(min < 60) {
            return String.format("%.2fmin", min);
        }

        double h = min / 60;

        return String.format("%.2fh", h);
    }
} // StopWatch
