/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.EnumConfigurable;
import com.oracle.labs.mlrg.olcut.config.PropertyException;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *
 */
public class EnumConfigurableTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test
    public void both() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("enumConfig.json");
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("both");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.B, ec.enum2);
    }

    @Test public void set1() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("enumConfig.json");
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("set1");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.B, ec.enum2);
        assertTrue(ec.enumSet1.contains(EnumConfigurable.Type.A), "Missing A");
        assertTrue(ec.enumSet1.contains(EnumConfigurable.Type.B), "Missing B");
        assertEquals(2, ec.enumSet1.size(), "Too big: " + ec.enumSet1);
    }

    @Test public void defaultSet() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("enumConfig.json");
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("both");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.B, ec.enum2);
        assertTrue(ec.enumSet1.contains(EnumConfigurable.Type.A), "Missing A");
        assertTrue(ec.enumSet1.contains(EnumConfigurable.Type.F), "Missing F");
        assertEquals(2, ec.enumSet1.size(), "Too big: " + ec.enumSet1);
    }

    @Test
    public void badSetValue() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("enumConfig.json");
            EnumConfigurable ec = (EnumConfigurable) cm.lookup("badset");
        });
    }

    @Test
    public void defaultValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("enumConfig.json");
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("default");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.A, ec.enum2);
    }

    @Test
    public void badValue() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("enumConfig.json");
            EnumConfigurable ec = (EnumConfigurable) cm.lookup("badvalue");
        });
    }

    @Test
    public void globalValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("enumConfig.json");
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("global");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.A, ec.enum2);
    }
    
}