package com.oracle.labs.mlrg.olcut.config;

import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertFalse(o.test);
        Assert.assertTrue(o.defaultTrue);
        Assert.assertFalse(o.defaultFalse);

        ConfigurationManager cm = new ConfigurationManager(args,o);

        Assert.assertTrue(o.test);
        Assert.assertFalse(o.defaultTrue);
        Assert.assertTrue(o.defaultFalse);
        Assert.assertEquals(0, cm.getUnnamedArguments().length);
    }

    @Test
    public void testLongBooleanOption() {
        String[] args = new String[]{"--bool-option-3", "true", "--bool-option-2", "false"};

        TestOptions o = new TestOptions();

        Assert.assertTrue(o.defaultTrue);
        Assert.assertFalse(o.defaultFalse);

        ConfigurationManager cm = new ConfigurationManager(args,o);

        Assert.assertFalse(o.defaultTrue);
        Assert.assertTrue(o.defaultFalse);
    }

    @Test
    public void testCombinationOptions() {
        String[] args = new String[]{"-adb","false"};

        TestOptions o = new TestOptions();

        ConfigurationManager cm = new ConfigurationManager(args,o);

        Assert.assertTrue(o.test);
        Assert.assertFalse(o.defaultTrue);
        Assert.assertTrue(o.defaultFalse);

        cm.close();

        args = new String[]{"-adb","true"};

        o = new TestOptions();

        cm = new ConfigurationManager(args,o);

        Assert.assertTrue(o.test);
        Assert.assertTrue(o.defaultTrue);
        Assert.assertTrue(o.defaultFalse);
    }

}
