package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DuplicatePropertyTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test(expected = ConfigLoaderException.class)
    public void duplicatePropertyMap() {
        ConfigurationManager cm = new ConfigurationManager("duplicatePropertyMapConfig.json");
        Assert.fail("Should have thrown exception on loading");
    }

    @Test(expected = ConfigLoaderException.class)
    public void duplicatePropertyList() {
        ConfigurationManager cm = new ConfigurationManager("duplicatePropertyListConfig.json");
        Assert.fail("Should have thrown exception on loading");
    }

    @Test(expected = ConfigLoaderException.class)
    public void duplicateProperty() {
        ConfigurationManager cm = new ConfigurationManager("duplicatePropertyConfig.json");
        Assert.fail("Should have thrown exception on loading");
    }
}
