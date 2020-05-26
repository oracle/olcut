
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
import java.util.concurrent.TimeUnit;

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

    /**
     * Returns a string version of the total accumulated time of this StopWatch, scaled
     * appropriately depending on the amount of time. The full time to the millisecond is
     * always shown, but the format varies depending on how much time is represented.
     * Times less than a minute return millisecond-precision seconds (s.mmm). Times between
     * a minute and a day return hh:MM:ss.mmm. Times greater than a day return a more
     * verbose string with each of days, hours, minute, seconds(.mmm) labeled.
     *
     * @return a string representing teh time
     */
    public String toString() {
        if(time < 1000) {
            return String.format("0.%03ds", time);
        }

        long secs = TimeUnit.MILLISECONDS.toSeconds(time);

        if(secs < 60) {
            return String.format("%d.%03ds", secs,
                                             time % TimeUnit.SECONDS.toMillis(1));
        }

        long min = TimeUnit.MILLISECONDS.toMinutes(time);

        if(min < 60) {
            return String.format("00:%02d:%02d.%03d", min,
                                                     TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1),
                                                     time % TimeUnit.SECONDS.toMillis(1));
        }

        long h = TimeUnit.MILLISECONDS.toHours(time);

        if (h < 24) {
            return String.format("%02d:%02d:%02d.%03d", h,
                    TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1),
                    time % TimeUnit.SECONDS.toMillis(1));
        }

        long d = TimeUnit.MILLISECONDS.toDays(time);

        return String.format("%d days %d hours %d mins %d.%03d seconds",
                d,
                TimeUnit.MILLISECONDS.toHours(time) % TimeUnit.DAYS.toHours(1),
                TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1),
                time % TimeUnit.SECONDS.toMillis(1));
    }

    /**
     * Creates a string representation to the nearest largest appropriate time unit.
     * @deprecated Use toString() instead which gives a more accurate, but still scoped appropriately time string
     * @param millis
     * @return
     */
    @Deprecated
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
