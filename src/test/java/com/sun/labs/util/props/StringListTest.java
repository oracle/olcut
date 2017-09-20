/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
        URL cu = getClass().getResource("stringListConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        StringListConfigurable slc = (StringListConfigurable) cm.lookup(
                "listTest");
        assertEquals("a", slc.strings.get(0));
        assertEquals("b", slc.strings.get(1));
        assertEquals("c", slc.strings.get(2));
    }
    
    @Test
    public void setStrings() throws IOException {
        URL cu = getClass().getResource("stringListConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        PropertySheet ps = cm.getPropertySheet("listTest");
        List<String> l = new ArrayList<>();
        l.add("d");
        l.add("e");
        l.add("f");
        ps.setProp("strings", l);
        StringListConfigurable slc = (StringListConfigurable) cm.lookup(
                "listTest");
        assertEquals("d", slc.strings.get(0));
        assertEquals("e", slc.strings.get(1));
        assertEquals("f", slc.strings.get(2));
    }

    @Test
    public void nastyStrings() throws IOException {
        URL cu = getClass().getResource("uglyStringConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        StringConfigurable sc = (StringConfigurable) cm.lookup(
                "regexTest");
        assertEquals("([^a-z0-9_!#$%&*@＠]|^|RT:?)(@＠+)([a-z0-9_]{1,20})(/[a-z][a-z0-9_\\-]{0,24})?", sc.one);
        assertEquals("@＠", sc.two);
        assertEquals("&&", sc.three);

    }
}