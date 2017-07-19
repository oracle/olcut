package com.sun.labs.util.props;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

/**
 *
 */
public class GenericConfigTest {

    @Test
    public void correctListConfig() throws IOException {
        URL cu = getClass().getResource("genericConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
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

        Assert.assertEquals("StringConfigList has an incorrect number of values", 2,s.stringConfigList.size());
        Assert.assertTrue("StringConfigList missing values",s.stringConfigList.contains(new StringConfig("dragons","wyverns","wyrms")));
        Assert.assertTrue("StringConfigList missing values",s.stringConfigList.contains(new StringConfig("jedi","sith","scoundrels")));
        Assert.assertFalse("StringConfigList contains incorrect values",s.stringConfigList.contains(new StringConfig("sheep","cows","pigs")));
    }

    @Test(expected=PropertyException.class)
    public void incorrectListConfig() throws IOException {
        URL cu = getClass().getResource("genericConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SetConfig s = (SetConfig) cm.lookup("incorrectListConfig");
    }

    @Test
    public void correctSetConfig() throws IOException {
        URL cu = getClass().getResource("genericConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
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

        Assert.assertEquals("StringConfigSet has an incorrect number of values", 2,s.stringConfigSet.size());
        Assert.assertTrue("StringConfigSet missing values",s.stringConfigSet.contains(new StringConfig("dragons","wyverns","wyrms")));
        Assert.assertTrue("StringConfigSet missing values",s.stringConfigSet.contains(new StringConfig("jedi","sith","scoundrels")));
        Assert.assertFalse("StringConfigSet contains incorrect values",s.stringConfigSet.contains(new StringConfig("sheep","cows","pigs")));
    }

    @Test(expected=PropertyException.class)
    public void incorrectSetConfig() throws IOException {
        URL cu = getClass().getResource("genericConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SetConfig s = (SetConfig) cm.lookup("incorrectSetConfig");
    }
}
