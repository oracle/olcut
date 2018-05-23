package com.oracle.labs.mlrg.olcut.config;

import org.junit.Assert;
import org.junit.Test;

public class DuplicatePropertyTest {

    @Test(expected = ConfigLoaderException.class)
    public void duplicatePropertyMap() {
        ConfigurationManager cm = new ConfigurationManager("duplicatePropertyMapConfig.xml");
        Assert.fail("Should have thrown exception on loading");
    }

    @Test(expected = ConfigLoaderException.class)
    public void duplicatePropertyList() {
        ConfigurationManager cm = new ConfigurationManager("duplicatePropertyListConfig.xml");
        Assert.fail("Should have thrown exception on loading");
    }

    @Test(expected = ConfigLoaderException.class)
    public void duplicateProperty() {
        ConfigurationManager cm = new ConfigurationManager("duplicatePropertyConfig.xml");
        Assert.fail("Should have thrown exception on loading");
    }
}
