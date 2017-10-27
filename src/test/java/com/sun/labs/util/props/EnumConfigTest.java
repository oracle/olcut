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
        EnumConfig ec = (EnumConfig) cm.lookup("both-config");
        assertEquals(EnumConfig.Type.A, ec.enum1);
        assertEquals(EnumConfig.Type.B, ec.enum2);
    }

    @Test public void set1() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        EnumConfig ec = (EnumConfig) cm.lookup("set1-config");
        assertEquals(EnumConfig.Type.A, ec.enum1);
        assertEquals(EnumConfig.Type.B, ec.enum2);
        assertTrue("Missing A", ec.enumSet1.contains(EnumConfig.Type.A));
        assertTrue("Missing B", ec.enumSet1.contains(EnumConfig.Type.B));
        assertTrue("Too big: " + ec.enumSet1, ec.enumSet1.size() == 2);
    }

    @Test public void defaultSet() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        EnumConfig ec = (EnumConfig) cm.lookup("both-config");
        assertEquals(EnumConfig.Type.A, ec.enum1);
        assertEquals(EnumConfig.Type.B, ec.enum2);
        assertTrue("Missing A", ec.enumSet1.contains(EnumConfig.Type.A));
        assertTrue("Missing F", ec.enumSet1.contains(EnumConfig.Type.F));
        assertTrue("Too big: " + ec.enumSet1, ec.enumSet1.size() == 2);
    }

    @Test(expected=PropertyException.class)
    public void badSetValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().
                getResource("enumConfig.xml"));
        EnumConfig ec = (EnumConfig) cm.lookup("badset-config");
    }

    @Test
    public void defaultValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfig ec = (EnumConfig) cm.lookup("default-config");
        assertEquals(EnumConfig.Type.A, ec.enum1);
        assertEquals(EnumConfig.Type.A, ec.enum2);
    }

    @Test(expected=PropertyException.class)
    public void badValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfig ec = (EnumConfig) cm.lookup("badvalue-config");
    }

    @Test
    public void globalValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(getClass().getResource("enumConfig.xml"));
        EnumConfig ec = (EnumConfig) cm.lookup("global-config");
        assertEquals(EnumConfig.Type.A, ec.enum1);
        assertEquals(EnumConfig.Type.A, ec.enum2);
    }
    
}