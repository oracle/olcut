package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DuplicateComponentFieldTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @Test(expected = ConfigLoaderException.class)
    public void duplicatePropertyMap() {
        ConfigurationManager cm = new ConfigurationManager("duplicatePropertyMapConfig.edn");
        Assert.fail("Should have thrown exception on loading");
    }

    @Test(expected = ConfigLoaderException.class)
    public void duplicatePropertyList() {
        ConfigurationManager cm = new ConfigurationManager("duplicatePropertyListConfig.edn");
        Assert.fail("Should have thrown exception on loading");
    }

    @Test(expected = ConfigLoaderException.class)
    public void duplicateProperty() {
        ConfigurationManager cm = new ConfigurationManager("duplicatePropertyConfig.edn");
        Assert.fail("Should have thrown exception on loading");
    }
}
