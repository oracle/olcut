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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Deprecated
public class GetoptTest {

    @BeforeAll
    public static void setup() {
        Logger logger = Logger.getLogger(Getopt.class.getName());
        logger.setLevel(Level.SEVERE);
    }

    @Test
    public void first() {
        String flags = "abc:d:xy";
        String[] argv = {"-ab", "-c", "carg", "-xy",
                "-cCARG", "--", "-a"};
        Getopt getopt = new Getopt(argv, flags);

        int c;
        while ((c = getopt.getopt()) != -1) {
            switch (c) {
                case 'a':
                    assertNull(getopt.optArg, "Found an argument for option a");
                    break;
                case 'b':
                    assertNull(getopt.optArg, "Found an argument for option b");
                    break;
                case 'c':
                    assertEquals("carg", getopt.optArg.toLowerCase(), "Found incorrect argument for option c");
                    break;
                case 'x':
                    assertNull(getopt.optArg, "Found an argument for option x");
                    break;
                case 'y':
                    assertNull(getopt.optArg, "Found an argument for option y");
                    break;
                default:
                    fail("Found an unknown argument - " + c);

            }
        }
        assertEquals( 6, getopt.optInd, "Found incorrect number of arguments");
    }

    @Test
    public void second() {
        String flags = "abc:d:xy";
        String[] argv = {"-ab", "carg", "kjh"};
        int c;
        Getopt getopt = new Getopt(argv, flags);
        getopt.optInd = 0;
        while ((c = getopt.getopt()) != -1) {
            switch (c) {
                case 'a':
                    assertNull(getopt.optArg, "Found an argument for option a");
                    break;
                case 'b':
                    assertNull(getopt.optArg, "Found an argument for option b");
                    break;
                case 'c':
                    fail("No argument c in input");
                    break;
                case 'x':
                    fail("No argument x in input");
                    break;
                case 'y':
                    fail("No argument y in input");
                    break;
                default:
                    fail("Found an unknown argument - " + c);
            }
        }
        assertEquals(1, getopt.optInd, "Found incorrect number of arguments");
    }

    @Test
    public void third() {
        String flags = "abc:d:xy";
        String[] argv = {"-ab", "-d", "-x", "carg", "kjh"};
        int c;
        Getopt getopt = new Getopt(argv, flags);
        getopt.optInd = 0;
        while ((c = getopt.getopt()) != -1) {
            switch (c) {
                case 'a':
                    assertNull(getopt.optArg, "Found an argument for option a");
                    break;
                case 'b':
                    assertNull(getopt.optArg, "Found an argument for option b");
                    break;
                case 'c':
                    fail("No argument c in input");
                    break;
                case 'd':
                    assertNotNull(getopt.optArg, "Found an argument for option d");
                    break;
                case 'x':
                    assertNull(getopt.optArg, "Found an argument for option x");
                    break;
                case 'y':
                    fail("No argument y in input");
                    break;
                default:
                    if (((char) c) == '?') {
                        assertEquals(3, getopt.optInd, "Found another missing argument");
                    } else {
                        fail("Found an unknown argument - " + c);
                    }
            }
        }

        assertEquals(3, getopt.optInd, "Found incorrect number of arguments");
    }

    @Test
    public void fourth() {
        String flags = "abc:d:xy";
        String[] argv = {"-ab", "-w", "-x", "carg", "kjh"};
        int c;
        Getopt getopt = new Getopt(argv, flags);
        getopt.optInd = 0;
        while ((c = getopt.getopt()) != -1) {
            switch (c) {
                case 'a':
                    assertNull(getopt.optArg, "Found an argument for option a");
                    break;
                case 'b':
                    assertNull(getopt.optArg, "Found an argument for option b");
                    break;
                case 'c':
                    fail("No argument c in input");
                    break;
                case 'd':
                    fail("No argument d in input");
                    break;
                case 'x':
                    assertNull(getopt.optArg, "Found an argument for option x");
                    break;
                case 'y':
                    fail("No argument y in input");
                    break;
                default:
                    if (((char) c) == '?') {
                        assertEquals(2, getopt.optInd, "Found unrecognised argument");
                    } else {
                        fail("Found an unknown argument - " + c);
                    }
            }
        }

        assertEquals(3, getopt.optInd, "Found incorrect number of arguments");
    }

}
