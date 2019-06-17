/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oracle.labs.mlrg.olcut.config;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author stgreen
 */
public class StringListTest {

    public StringListTest() {
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
    public void getStrings() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringListConfig.xml");
        StringListConfigurable slc = (StringListConfigurable) cm.lookup(
                "listTest");
        assertEquals("a", slc.strings.get(0));
        assertEquals("b", slc.strings.get(1));
        assertEquals("c", slc.strings.get(2));
    }
    
    @Test
    public void setStrings() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringListConfig.xml");
        PropertySheet ps = cm.getPropertySheet("listTest");
        List<String> l = new ArrayList<>();
        l.add("d");
        l.add("e");
        l.add("f");
        ps.setProp("strings", ListProperty.createFromStringList(l));
        StringListConfigurable slc = (StringListConfigurable) cm.lookup(
                "listTest");
        assertEquals("d", slc.strings.get(0));
        assertEquals("e", slc.strings.get(1));
        assertEquals("f", slc.strings.get(2));
    }

}