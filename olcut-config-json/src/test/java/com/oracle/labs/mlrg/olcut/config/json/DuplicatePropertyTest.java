package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DuplicatePropertyTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test
    public void duplicatePropertyMap() {
        assertThrows(ConfigLoaderException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("duplicatePropertyMapConfig.json");
        }, "Should have thrown exception on loading");
    }

    @Test
    public void duplicatePropertyList() {
        assertThrows(ConfigLoaderException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("duplicatePropertyListConfig.json");
        }, "Should have thrown exception on loading");
    }

    @Test
    public void duplicateProperty() {
        assertThrows(ConfigLoaderException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("duplicatePropertyConfig.json");
        }, "Should have thrown exception on loading");
    }
}
