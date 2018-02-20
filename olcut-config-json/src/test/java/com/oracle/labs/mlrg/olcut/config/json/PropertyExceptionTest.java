package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.SimpleConfigurable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 *
 * @author stgreen
 */
public class PropertyExceptionTest {

    public PropertyExceptionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * A test that will throw a property exception due to an unknown property
     * in the configuration file.
     * @throws PropertyException
     * @throws IOException
     */
    @Test(expected=PropertyException.class)
    public void unknownPropertyException() throws PropertyException, IOException {
        ConfigurationManager cm = new ConfigurationManager("undefinedPropertyConfig.json");
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simple");
    }
    
    @Test(expected=PropertyException.class)
    public void unknownPropertyWithKnownPropertyException() throws PropertyException, IOException {
        ConfigurationManager cm = new ConfigurationManager("undefinedPropertyConfig.json");
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simple2");
    }
}