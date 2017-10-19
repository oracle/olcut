package com.oracle.labs.mlrg.olcut;

import com.oracle.labs.mlrg.olcut.util.Getopt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
                    assertTrue("Found an argument for option a",getopt.optArg == null);
                    break;
                case 'b':
                    assertTrue("Found an argument for option b", getopt.optArg == null);
                    break;
                case 'c':
                    assertEquals("Found incorrect argument for option c", "carg", getopt.optArg.toLowerCase());
                    break;
                case 'x':
                    assertTrue("Found an argument for option x", getopt.optArg == null);
                    break;
                case 'y':
                    assertTrue("Found an argument for option y", getopt.optArg == null);
                    break;
                default:
                    fail("Found an unknown argument - " + c);

            }
        }
        assertEquals("Found incorrect number of arguments", 6, getopt.optInd);
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
                    assertTrue("Found an argument for option a",getopt.optArg == null);
                    break;
                case 'b':
                    assertTrue("Found an argument for option b", getopt.optArg == null);
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
        assertEquals("Found incorrect number of arguments", 1, getopt.optInd);
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
                    assertTrue("Found an argument for option a",getopt.optArg == null);
                    break;
                case 'b':
                    assertTrue("Found an argument for option b", getopt.optArg == null);
                    break;
                case 'c':
                    fail("No argument c in input");
                    break;
                case 'd':
                    assertFalse("Found an argument for option d", getopt.optArg == null);
                    break;
                case 'x':
                    assertTrue("Found an argument for option x", getopt.optArg == null);
                    break;
                case 'y':
                    fail("No argument y in input");
                    break;
                default:
                    if (((char) c) == '?') {
                        assertTrue("Found another missing argument", getopt.optInd == 3);
                    } else {
                        fail("Found an unknown argument - " + c);
                    }
            }
        }

        assertEquals("Found incorrect number of arguments", 3, getopt.optInd);
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
                    assertTrue("Found an argument for option a",getopt.optArg == null);
                    break;
                case 'b':
                    assertTrue("Found an argument for option b", getopt.optArg == null);
                    break;
                case 'c':
                    fail("No argument c in input");
                    break;
                case 'd':
                    fail("No argument d in input");
                    break;
                case 'x':
                    assertTrue("Found an argument for option x", getopt.optArg == null);
                    break;
                case 'y':
                    fail("No argument y in input");
                    break;
                default:
                    if (((char) c) == '?') {
                        assertTrue("Found unrecognised argument", getopt.optInd == 2);
                    } else {
                        fail("Found an unknown argument - " + c);
                    }
            }
        }

        assertEquals("Found incorrect number of arguments", 3, getopt.optInd);
    }

}
