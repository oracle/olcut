package com.oracle.labs.mlrg.olcut.config;


import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class SimpleFieldTest {
    @Test
    public void testCharacterFields() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("simpleFieldConfig.xml");
        SimpleFieldConfigurable sfc = (SimpleFieldConfigurable) cm.lookup(
                "fieldTest");
        assertEquals('a', sfc.charField);
        assertEquals((Character)'b', sfc.characterField);
    }

    @Test
    public void testInvalidCharacterFields() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("simpleFieldConfig.xml");
            SimpleFieldConfigurable sfc = (SimpleFieldConfigurable) cm.lookup(
                    "invalidCharacterFieldTest");
        });
    }
}
