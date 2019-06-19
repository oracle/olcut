package com.oracle.labs.mlrg.olcut.util;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class GetoptTest {

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
                    assertTrue(getopt.optArg == null, "Found an argument for option a");
                    break;
                case 'b':
                    assertTrue(getopt.optArg == null, "Found an argument for option b");
                    break;
                case 'c':
                    assertEquals("carg", getopt.optArg.toLowerCase(), "Found incorrect argument for option c");
                    break;
                case 'x':
                    assertTrue(getopt.optArg == null, "Found an argument for option x");
                    break;
                case 'y':
                    assertTrue(getopt.optArg == null, "Found an argument for option y");
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
                    assertTrue(getopt.optArg == null, "Found an argument for option a");
                    break;
                case 'b':
                    assertTrue( getopt.optArg == null, "Found an argument for option b");
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
                    assertTrue(getopt.optArg == null, "Found an argument for option a");
                    break;
                case 'b':
                    assertTrue(getopt.optArg == null, "Found an argument for option b");
                    break;
                case 'c':
                    fail("No argument c in input");
                    break;
                case 'd':
                    assertFalse(getopt.optArg == null, "Found an argument for option d");
                    break;
                case 'x':
                    assertTrue(getopt.optArg == null, "Found an argument for option x");
                    break;
                case 'y':
                    fail("No argument y in input");
                    break;
                default:
                    if (((char) c) == '?') {
                        assertTrue(getopt.optInd == 3, "Found another missing argument");
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
                    assertTrue(getopt.optArg == null, "Found an argument for option a");
                    break;
                case 'b':
                    assertTrue(getopt.optArg == null, "Found an argument for option b");
                    break;
                case 'c':
                    fail("No argument c in input");
                    break;
                case 'd':
                    fail("No argument d in input");
                    break;
                case 'x':
                    assertTrue(getopt.optArg == null, "Found an argument for option x");
                    break;
                case 'y':
                    fail("No argument y in input");
                    break;
                default:
                    if (((char) c) == '?') {
                        assertTrue(getopt.optInd == 2, "Found unrecognised argument");
                    } else {
                        fail("Found an unknown argument - " + c);
                    }
            }
        }

        assertEquals(3, getopt.optInd, "Found incorrect number of arguments");
    }

}
