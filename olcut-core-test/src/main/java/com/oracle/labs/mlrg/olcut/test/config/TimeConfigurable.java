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

package com.oracle.labs.mlrg.olcut.test.config;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Objects;

/**
 *
 */
public final class TimeConfigurable implements Configurable {

    @Config(mandatory=true)
    public LocalDate date;

    @Config(mandatory=true)
    public OffsetDateTime dateTime;

    @Config(mandatory=true)
    public OffsetTime time;

    private TimeConfigurable() {}

    public TimeConfigurable(LocalDate date, OffsetDateTime dateTime, OffsetTime time) {
        this.date = date;
        this.dateTime = dateTime;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeConfigurable)) return false;
        TimeConfigurable that = (TimeConfigurable) o;
        return date.equals(that.date) &&
                dateTime.equals(that.dateTime) &&
                time.equals(that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, dateTime, time);
    }
}
