/*
 * Copyright 2007-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */
package com.sun.labs.util;

import java.io.Serializable;

/**
 * A class implementing a simple stop watch that can be used for timing.
 */
public class StopWatch implements Serializable {

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
    public int getClicks() {
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
    protected int clicks;

    /**
     * The last start time.
     */
    protected long lastStart;

} // StopWatch
