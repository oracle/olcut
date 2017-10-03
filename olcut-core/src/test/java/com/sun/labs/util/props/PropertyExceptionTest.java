package com.sun.labs.util.props;

import java.io.IOException;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
     * @throws com.sun.labs.util.props.PropertyException
     * @throws java.io.IOException
     */
    @Test(expected=com.sun.labs.util.props.PropertyException.class)
    public void unknownPropertyException() throws PropertyException, IOException {
        URL cu = getClass().getResource("undefinedPropertyConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simple");
    }
    
    @Test(expected=com.sun.labs.util.props.PropertyException.class)
    public void unknownPropertyWithKnownPropertyException() throws PropertyException, IOException {
        URL cu = getClass().getResource("undefinedPropertyConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simple2");
    }
}