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

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.test.config.TimeConfigurable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class TimeTest {

    @Test
    public void testValid() {
        ConfigurationManager cm = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/test/config/timeConfig.xml");
        TimeConfigurable time = (TimeConfigurable) cm.lookup("valid-time");
        Assertions.assertEquals(OffsetTime.parse("12:34+00:00"),time.time);
        Assertions.assertEquals(LocalDate.parse("1066-10-04"),time.date);
        Assertions.assertEquals(OffsetDateTime.parse("1949-06-16T20:30:00+01:00"),time.dateTime);
    }

    @Test
    public void testInvalidTime() {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/test/config/timeConfig.xml");
            TimeConfigurable time = (TimeConfigurable) cm.lookup("invalid-time");
        });
    }

    @Test
    public void testInvalidDate() {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/test/config/timeConfig.xml");
            TimeConfigurable time = (TimeConfigurable) cm.lookup("invalid-date");
        });
    }

    @Test
    public void testInvalidDateTime() {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/test/config/timeConfig.xml");
            TimeConfigurable time = (TimeConfigurable) cm.lookup("invalid-date-time");
        });
    }

}
