
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
