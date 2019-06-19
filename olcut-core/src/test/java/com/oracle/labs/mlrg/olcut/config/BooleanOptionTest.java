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
