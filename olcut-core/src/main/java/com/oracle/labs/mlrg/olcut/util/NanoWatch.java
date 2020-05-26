
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
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * A nanosecond stop watch.
 */
public class NanoWatch extends StopWatch implements Serializable {
    public static final DecimalFormat nanoPartFormat = new DecimalFormat("#,###");

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

    /**
     * Get the total completed time in this NanoWatch in nanoseconds
     *
     * @return the total time in nanos
     */
    public long getTimeNanos() {
        return time;
    }

    /**
     * Get the most recent completed time in nanoseconds
     *
     * @return the most recent time in nanos
     */
    public long getLastTimeNanos() {
        return lastTime;
    }

    /**
     * Gets the time that this NanoWatch last started in nanoseconds since the Epoch
     *
     * @return the last start time in nanoseconds since the Epoch
     */
    public long getLastStartNanos() {
        return lastStart;
    }

    /**
     * Gets the average length of time per start/stop cycle of this NanoWatch in nanoseconds
     *
     * @return the average time per click of this NanoWatch in nanos
     */
    public double getAvgTimeNanos() {
        return getTimeNanos() / (double)clicks;
    }

    /**
     * Returns a string version of the total accumulated time of this StopWatch, scaled
     * appropriately depending on the amount of time. The full time to the nanosecond is
     * always shown, but the format varies depending on how much time is represented.
     * Times less than a second returns the number of nanoseconds. Times between
     * a second and a day return hh:mm:ss and nanos. Times greater than a day add a
     * number of days preceding the previous format.
     *
     * @return a string representing the time
     */
    @Override
    public String toString() {
        return formatNanosecondTime(time);
    }

    public static final String formatNanosecondTime(long nanos) {
        //
        // Are we less than a second?
        if (nanos < TimeUnit.SECONDS.toNanos(1)) {
            return String.format("%s nanos", nanoPartFormat.format(nanos));
        }

        //
        // More than a second but less than a day?
        if (nanos < TimeUnit.DAYS.toNanos(1)) {
            return String.format("%02d:%02d:%02d and %s nanos",
                    TimeUnit.NANOSECONDS.toHours(nanos),
                    TimeUnit.NANOSECONDS.toMinutes(nanos) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.NANOSECONDS.toSeconds(nanos) % TimeUnit.MINUTES.toSeconds(1),
                    nanoPartFormat.format(nanos % TimeUnit.SECONDS.toNanos(1)));
        }

        //
        // More than a day
        return String.format("%d days %02d:%02d:%02d and %s nanos",
                TimeUnit.NANOSECONDS.toDays(nanos),
                TimeUnit.NANOSECONDS.toHours(nanos) % TimeUnit.DAYS.toHours(1),
                TimeUnit.NANOSECONDS.toMinutes(nanos) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.NANOSECONDS.toSeconds(nanos) % TimeUnit.MINUTES.toSeconds(1),
                nanoPartFormat.format(nanos % TimeUnit.SECONDS.toNanos(1)));
    }

    @Override
    public TimeUnit getUnit() {
        return TimeUnit.NANOSECONDS;
    }
}
