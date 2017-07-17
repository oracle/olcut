/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author stgreen
 */
public class ConfigEnumConfigTest {

    public ConfigEnumConfigTest() {
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
        ConfigEnumConfigurable ec = (ConfigEnumConfigurable) cm.lookup("both-config");
        assertEquals(ConfigEnumConfigurable.Type.A, ec.enum1);
        assertEquals(ConfigEnumConfigurable.Type.B, ec.enum2);
    }

    @Test public void set1() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        ConfigEnumConfigurable ec = (ConfigEnumConfigurable) cm.lookup("set1-config");
        assertEquals(ConfigEnumConfigurable.Type.A, ec.enum1);
        assertEquals(ConfigEnumConfigurable.Type.B, ec.enum2);
        assertTrue("Missing A", ec.enumSet1.contains(ConfigEnumConfigurable.Type.A));
        assertTrue("Missing B", ec.enumSet1.contains(ConfigEnumConfigurable.Type.B));
        assertTrue("Too big: " + ec.enumSet1, ec.enumSet1.size() == 2);
    }

    @Test public void defaultSet() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        ConfigEnumConfigurable ec = (ConfigEnumConfigurable) cm.lookup("both-config");
        assertEquals(ConfigEnumConfigurable.Type.A, ec.enum1);
        assertEquals(ConfigEnumConfigurable.Type.B, ec.enum2);
        assertTrue("Missing A", ec.enumSet1.contains(ConfigEnumConfigurable.Type.A));
        assertTrue("Missing F", ec.enumSet1.contains(ConfigEnumConfigurable.Type.F));
        assertTrue("Too big: " + ec.enumSet1, ec.enumSet1.size() == 2);
    }

    @Test(expected=PropertyException.class)
    public void badSetValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        ConfigEnumConfigurable ec = (ConfigEnumConfigurable) cm.lookup("badset-config");
    }

    @Test
    public void defaultValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        ConfigEnumConfigurable ec = (ConfigEnumConfigurable) cm.lookup("default-config");
        assertEquals(ConfigEnumConfigurable.Type.A, ec.enum1);
        assertEquals(ConfigEnumConfigurable.Type.A, ec.enum2);
    }

    @Test(expected=PropertyException.class)
    public void badValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        ConfigEnumConfigurable ec = (ConfigEnumConfigurable) cm.lookup("badvalue-config");
    }

    @Test
    public void globalValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        ConfigEnumConfigurable ec = (ConfigEnumConfigurable) cm.lookup("global-config");
        assertEquals(ConfigEnumConfigurable.Type.A, ec.enum1);
        assertEquals(ConfigEnumConfigurable.Type.A, ec.enum2);
    }
    
}