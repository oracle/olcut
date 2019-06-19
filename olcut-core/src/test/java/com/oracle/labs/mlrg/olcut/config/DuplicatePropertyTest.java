package com.oracle.labs.mlrg.olcut.config;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DuplicatePropertyTest {

    @Test
    public void duplicatePropertyMap() {
        assertThrows(ConfigLoaderException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("duplicatePropertyMapConfig.xml");
        }, "Should have thrown exception on loading");
    }

    @Test
    public void duplicatePropertyList() {
        assertThrows(ConfigLoaderException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("duplicatePropertyListConfig.xml");
        }, "Should have thrown exception on loading");
    }

    @Test
    public void duplicateProperty() {
        assertThrows(ConfigLoaderException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("duplicatePropertyConfig.xml");
        }, "Should have thrown exception on loading");
    }
}
