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
public class EnumConfigurableTest {

    public EnumConfigurableTest() {
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
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.B, ec.enum2);
    }

    @Test public void set1() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("set1");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.B, ec.enum2);
        assertTrue("Missing A", ec.enumSet1.contains(EnumConfigurable.Type.A));
        assertTrue("Missing B", ec.enumSet1.contains(EnumConfigurable.Type.B));
        assertTrue("Too big: " + ec.enumSet1, ec.enumSet1.size() == 2);
    }

    @Test public void defaultSet() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("both");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.B, ec.enum2);
        assertTrue("Missing A", ec.enumSet1.contains(EnumConfigurable.Type.A));
        assertTrue("Missing F", ec.enumSet1.contains(EnumConfigurable.Type.F));
        assertTrue("Too big: " + ec.enumSet1, ec.enumSet1.size() == 2);
    }

    @Test(expected=PropertyException.class)
    public void badSetValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("badset");
    }

    @Test
    public void defaultValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("default");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.A, ec.enum2);
    }

    @Test(expected=PropertyException.class)
    public void badValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("badvalue");
    }

    @Test
    public void globalValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("global");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.A, ec.enum2);
    }
    
}