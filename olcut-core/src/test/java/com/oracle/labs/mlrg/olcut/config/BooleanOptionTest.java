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

package com.oracle.labs.mlrg.olcut.config;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class BooleanOptionTest {

    public static class TestOptions implements Options {
        @Option(charName='a',longName="bool-option",usage="Boolean option")
        public boolean test;
        @Option(charName='b',longName="bool-option-2",usage="Boolean option, default to true")
        public boolean defaultTrue = true;
        @Option(charName='d',longName="bool-option-3",usage="Boolean option, default to false")
        public boolean defaultFalse = false;
    }

    @Test
    public void testShortBooleanOption() {
        String[] args = new String[]{"-a", "-d", "true", "-b", "false"};

        TestOptions o = new TestOptions();

        assertFalse(o.test);
        assertTrue(o.defaultTrue);
        assertFalse(o.defaultFalse);

        ConfigurationManager cm = new ConfigurationManager(args,o);

        assertTrue(o.test);
        assertFalse(o.defaultTrue);
        assertTrue(o.defaultFalse);
        assertEquals(0, cm.getUnnamedArguments().length);
    }

    @Test
    public void testLongBooleanOption() {
        String[] args = new String[]{"--bool-option-3", "true", "--bool-option-2", "false"};

        TestOptions o = new TestOptions();

        assertTrue(o.defaultTrue);
        assertFalse(o.defaultFalse);

        ConfigurationManager cm = new ConfigurationManager(args,o);

        assertFalse(o.defaultTrue);
        assertTrue(o.defaultFalse);
    }

    @Test
    public void testCombinationOptions() {
        String[] args = new String[]{"-adb","false"};

        TestOptions o = new TestOptions();

        ConfigurationManager cm = new ConfigurationManager(args,o);

        assertTrue(o.test);
        assertFalse(o.defaultTrue);
        assertTrue(o.defaultFalse);

        cm.close();

        args = new String[]{"-adb","true"};

        o = new TestOptions();

        cm = new ConfigurationManager(args,o);

        assertTrue(o.test);
        assertTrue(o.defaultTrue);
        assertTrue(o.defaultFalse);
    }

}
