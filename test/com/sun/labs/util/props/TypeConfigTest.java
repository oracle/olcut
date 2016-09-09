package com.sun.labs.util.props;

import java.io.IOException;
import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A test for using the @Config annotation.
 */
public class TypeConfigTest {
    
    public TypeConfigTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void configuredTypes() throws IOException {
        URL cu = getClass().getResource("basicTypeConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        BasicTypeConfigurable bc1 = (BasicTypeConfigurable) cm1.lookup("a");
        assertEquals(bc1.s, "one");
        assertEquals(bc1.i, 2);
        assertEquals(bc1.d, 3.0, 1E-9);
        bc1 = (BasicTypeConfigurable) cm1.lookup("b");
        assertEquals(bc1.s, "two");
        assertEquals(bc1.i, 3);
        assertEquals(bc1.d, 6.3, 1E-9);
    }
}
