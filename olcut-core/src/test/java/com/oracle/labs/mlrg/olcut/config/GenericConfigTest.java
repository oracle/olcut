package com.oracle.labs.mlrg.olcut.config;

import java.io.IOException;
import java.util.List;

import com.oracle.labs.mlrg.olcut.config.test.Ape;
import com.oracle.labs.mlrg.olcut.config.test.Monkey;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class GenericConfigTest {

    @SuppressWarnings("unchecked")//Looking up a specific class via it's full name
    @Test
    public void lookupAllTest() throws ClassNotFoundException {
        ConfigurationManager cm = new ConfigurationManager("genericConfig.xml");

        Class<Monkey> clazz = (Class<Monkey>) Class.forName("com.oracle.labs.mlrg.olcut.config.test.Monkey");
        List<Monkey> monkeys = cm.lookupAll(clazz);

        Assert.assertEquals("Didn't find all the MonkeyConfigurable classes", 5, monkeys.size());

        Class<Ape> apeClazz = (Class<Ape>) Class.forName("com.oracle.labs.mlrg.olcut.config.test.Ape");
        List<Ape> apes = cm.lookupAll(apeClazz);

        Assert.assertEquals("Didn't find all the Ape classes", 3, apes.size());
    }

    @Test
    public void correctListConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("genericConfig.xml");
        ListConfig s = (ListConfig) cm.lookup("correctListConfig");

        Assert.assertEquals("StringList has an incorrect number of values", 4,s.stringList.size());
        Assert.assertTrue("StringList missing values",s.stringList.contains("5.5"));
        Assert.assertTrue("StringList missing values",s.stringList.contains("3.14"));
        Assert.assertTrue("StringList missing values",s.stringList.contains("2.79"));
        Assert.assertFalse("StringList contains incorrect value",s.stringList.contains("1000000.0"));

        Assert.assertEquals("DoubleList has an incorrect number of values", 4,s.doubleList.size());
        Assert.assertTrue("DoubleList missing values",s.doubleList.contains(5.5));
        Assert.assertTrue("DoubleList missing values",s.doubleList.contains(3.14));
        Assert.assertTrue("DoubleList missing values",s.doubleList.contains(2.79));
        Assert.assertFalse("DoubleList contains incorrect values",s.doubleList.contains(1000000.0));

        Assert.assertEquals("StringConfigList has an incorrect number of values", 2,s.stringConfigurableList.size());
        Assert.assertTrue("StringConfigList missing values",s.stringConfigurableList.contains(new StringConfigurable("dragons","wyverns","wyrms")));
        Assert.assertTrue("StringConfigList missing values",s.stringConfigurableList.contains(new StringConfigurable("jedi","sith","scoundrels")));
        Assert.assertFalse("StringConfigList contains incorrect values",s.stringConfigurableList.contains(new StringConfigurable("sheep","cows","pigs")));
    }

    @Test(expected=PropertyException.class)
    public void incorrectListConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("genericConfig.xml");
        ListConfig l = (ListConfig) cm.lookup("incorrectListConfig");
    }

    @Test
    public void correctSetConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("genericConfig.xml");
        SetConfig s = (SetConfig) cm.lookup("correctSetConfig");

        Assert.assertEquals("StringSet has an incorrect number of values", 3,s.stringSet.size());
        Assert.assertTrue("StringSet missing values",s.stringSet.contains("5.5"));
        Assert.assertTrue("StringSet missing values",s.stringSet.contains("3.14"));
        Assert.assertTrue("StringSet missing values",s.stringSet.contains("2.79"));
        Assert.assertFalse("StringSet contains incorrect value",s.stringSet.contains("1000000.0"));

        Assert.assertEquals("DoubleSet has an incorrect number of values", 3,s.doubleSet.size());
        Assert.assertTrue("DoubleSet missing values",s.doubleSet.contains(5.5));
        Assert.assertTrue("DoubleSet missing values",s.doubleSet.contains(3.14));
        Assert.assertTrue("DoubleSet missing values",s.doubleSet.contains(2.79));
        Assert.assertFalse("DoubleSet contains incorrect values",s.doubleSet.contains(1000000.0));

        Assert.assertEquals("StringConfigSet has an incorrect number of values", 2,s.stringConfigurableSet.size());
        Assert.assertTrue("StringConfigSet missing values",s.stringConfigurableSet.contains(new StringConfigurable("dragons","wyverns","wyrms")));
        Assert.assertTrue("StringConfigSet missing values",s.stringConfigurableSet.contains(new StringConfigurable("jedi","sith","scoundrels")));
        Assert.assertFalse("StringConfigSet contains incorrect values",s.stringConfigurableSet.contains(new StringConfigurable("sheep","cows","pigs")));
    }

    @Test(expected=PropertyException.class)
    public void incorrectSetConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("genericConfig.xml");
        SetConfig s = (SetConfig) cm.lookup("incorrectSetConfig");
    }
}
