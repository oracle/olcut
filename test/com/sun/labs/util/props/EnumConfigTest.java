/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stgreen
 */
public class EnumConfigTest {

    public EnumConfigTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void both() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("both");
        assertEquals(EnumConfigurable.Type.A, ec.one);
        assertEquals(EnumConfigurable.Type.B, ec.two);
    }

    @Test public void set1() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("set1");
        assertEquals(EnumConfigurable.Type.A, ec.one);
        assertEquals(EnumConfigurable.Type.B, ec.two);
        assertTrue("Missing A", ec.sone.contains(EnumConfigurable.Type.A));
        assertTrue("Missing B", ec.sone.contains(EnumConfigurable.Type.B));
        assertTrue("Too big: " + ec.sone, ec.sone.size() == 2);
    }

    @Test public void defaultSet() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("both");
        assertEquals(EnumConfigurable.Type.A, ec.one);
        assertEquals(EnumConfigurable.Type.B, ec.two);
        assertTrue("Missing A", ec.sone.contains(EnumConfigurable.Type.A));
        assertTrue("Missing F", ec.sone.contains(EnumConfigurable.Type.F));
        assertTrue("Too big: " + ec.sone, ec.sone.size() == 2);
    }

    @Test(expected =
    com.sun.labs.util.props.InternalConfigurationException.class)
    public void badSetValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("badset");
    }

    @Test
    public void defaultValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("default");
        assertEquals(EnumConfigurable.Type.A, ec.one);
        assertEquals(EnumConfigurable.Type.A, ec.two);
    }
    
    @Test(expected=com.sun.labs.util.props.InternalConfigurationException.class)
    public void noRequired() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("norequired");
    }

    @Test(expected=com.sun.labs.util.props.InternalConfigurationException.class)
    public void badValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("badvalue");
    }

    @Test
    public void globalValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("global");
        assertEquals(EnumConfigurable.Type.A, ec.one);
        assertEquals(EnumConfigurable.Type.A, ec.two);
    }
    
}