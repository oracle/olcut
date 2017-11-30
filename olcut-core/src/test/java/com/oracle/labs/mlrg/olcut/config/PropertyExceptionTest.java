package com.oracle.labs.mlrg.olcut.config;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author stgreen
 */
public class PropertyExceptionTest {

    public PropertyExceptionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * A test that will throw a property exception due to an unknown property
     * in the configuration file.
     * @throws PropertyException
     * @throws java.io.IOException
     */
    @Test(expected=PropertyException.class)
    public void unknownPropertyException() throws PropertyException, IOException {
        ConfigurationManager cm = new ConfigurationManager("undefinedPropertyConfig.xml");
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simple");
    }
    
    @Test(expected=PropertyException.class)
    public void unknownPropertyWithKnownPropertyException() throws PropertyException, IOException {
        ConfigurationManager cm = new ConfigurationManager("undefinedPropertyConfig.xml");
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simple2");
    }
}