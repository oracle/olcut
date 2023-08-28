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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class InvalidOptionsTest {

    public static class FooOptions implements Options {
        public BarOptions bar;
        public BazOptions baz;

        @Option(longName="test",usage="test")
        public String test;
        @Option(longName="foo",usage="foo")
        public String foo;

        @Override
        public String toString() {
            return "FooOptions{" +
                    "bar=" + bar +
                    ", baz=" + baz +
                    ", test='" + test + '\'' +
                    ", foo='" + foo + '\'' +
                    '}';
        }
    }

    public static class BarOptions implements Options {
        public BazOptions baz;

        @Option(longName="bar",usage="bar")
        public String bar;

        @Override
        public String toString() {
            return "BarOptions{" +
                    "baz=" + baz +
                    ", bar='" + bar + '\'' +
                    '}';
        }
    }

    public static class BazOptions implements Options {
        @Option(longName="baz",usage="baz")
        public String baz;
        @Option(longName="baz2bazharder",usage="baz2bazharder")
        public String baz2bazharder;

        @Override
        public String toString() {
            return "BazOptions{" +
                    "baz='" + baz + '\'' +
                    ", baz2bazharder='" + baz2bazharder + '\'' +
                    '}';
        }
    }

    public static class QuuxOptions implements Options {
        public BazOptions baz;
        public BazOptions baz2;

        @Option(longName="quux",usage="quux")
        public String quux;

        @Override
        public String toString() {
            return "QuuxOptions{" +
                    "baz=" + baz +
                    ", baz2=" + baz2 +
                    ", quux='" + quux + '\'' +
                    '}';
        }
    }

    @Test
    public void invalidNestingTest() {
        FooOptions foo = new FooOptions();

        String[] args = new String[]{"--foo","fooy","--baz","bazzy"};

        try {
            ConfigurationManager cm = new ConfigurationManager(args,foo);
            fail("Should have thrown an ArgumentException");
        } catch (ArgumentException e) {
            //pass
            assertTrue(e.getMessage().contains("two instances of"));
            assertTrue(e.getMessage().contains("com.oracle.labs.mlrg.olcut.config.InvalidOptionsTest$BazOptions"));
        }
    }

    @Test
    public void invalidDoublingTest() {
        QuuxOptions quux = new QuuxOptions();

        String[] args = new String[]{"--quux","quuxy","--baz","bazzy"};

        try {
            ConfigurationManager cm = new ConfigurationManager(args,quux);
            fail("Should have thrown an ArgumentException");
        } catch (ArgumentException e) {
            //pass
            assertTrue(e.getMessage().contains("two instances of"));
            assertTrue(e.getMessage().contains("com.oracle.labs.mlrg.olcut.config.InvalidOptionsTest$BazOptions"));
        }
    }

}
