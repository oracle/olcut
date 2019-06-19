package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.SimpleConfigurable;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class PropertyExceptionTest {

    public PropertyExceptionTest() {
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    /**
     * A test that will throw a property exception due to an unknown property
     * in the configuration file.
     * @throws PropertyException
     * @throws IOException
     */
    @Test
    public void unknownPropertyException() throws PropertyException, IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("undefinedPropertyConfig.json");
            SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simple");
        });
    }
    
    @Test
    public void unknownPropertyWithKnownPropertyException() throws PropertyException, IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("undefinedPropertyConfig.json");
            SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simple2");
        });
    }
}