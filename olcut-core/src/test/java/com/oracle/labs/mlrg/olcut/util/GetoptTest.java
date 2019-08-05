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
