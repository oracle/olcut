package com.oracle.labs.mlrg.olcut.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 *
 */
public class PropertyExceptionTest {

    public PropertyExceptionTest() {
    }


    /**
     * A test that will throw a property exception due to an unknown property
     * in the configuration file.
     * @throws PropertyException
     * @throws java.io.IOException
     */
    @Test
    public void unknownPropertyException() throws PropertyException, IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("undefinedPropertyConfig.xml");
            SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simple");
        });
    }
    
    @Test
    public void unknownPropertyWithKnownPropertyException() throws PropertyException, IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("undefinedPropertyConfig.xml");
            SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simple2");
        });
    }
}