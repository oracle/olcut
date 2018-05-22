package com.oracle.labs.mlrg.olcut.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 *
 */
public class SimpleFieldTest {
    @Test
    public void testCharacterFields() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("simpleFieldConfig.xml");
        SimpleFieldConfigurable sfc = (SimpleFieldConfigurable) cm.lookup(
                "fieldTest");
        Assert.assertEquals('a', sfc.charField);
        Assert.assertEquals((Character)'b', sfc.characterField);
    }

    @Test(expected = PropertyException.class)
    public void testInvalidCharacterFields() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("simpleFieldConfig.xml");
        SimpleFieldConfigurable sfc = (SimpleFieldConfigurable) cm.lookup(
                "invalidCharacterFieldTest");
        Assert.assertEquals('a', sfc.charField);
        Assert.assertEquals((Character)'b', sfc.characterField);
    }
}
