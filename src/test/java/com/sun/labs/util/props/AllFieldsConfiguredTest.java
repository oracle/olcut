package com.sun.labs.util.props;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests reading and writing all valid fields from a config file.
 */
public class AllFieldsConfiguredTest {

    File f;

    @Before
    public void setUp() throws IOException {
        f = File.createTempFile("all-config", ".xml");
        //f.deleteOnExit();
    }

    @Test
    public void loadConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("allConfig.xml"));
        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup("all-config");
        assertTrue("Failed to load all-config",ac!=null);
    }

    @Test
    public void saveConfig() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager(getClass().getResource("allConfig.xml"));
        AllFieldsConfigurable ac1 = (AllFieldsConfigurable) cm1.lookup("all-config");
        cm1.save(f, true);
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        assertEquals("Two all configs aren't equal",ac1,ac2);
    }

}
