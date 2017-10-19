/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.labs.mlrg.olcut.config;

import java.io.File;
import java.util.Map;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sg93990
 */
public class ProgrammaticConfigurableTest {

    private static final Logger logger = Logger.getLogger(ProgrammaticConfigurableTest.class.getName());

    public ProgrammaticConfigurableTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger l = Logger.getLogger("");
        for(Handler h : l.getHandlers()) {
            h.setLevel(Level.FINER);
        }
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

    /**
     * Tests adding a configurable with the default properties.
     */
    @Test
    public void addDefaultStringConfigurable() throws IOException {
        URL cu = getClass().getResource("stringConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        cm.addConfigurable(StringConfigurable.class, "c");
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("", sc.one);
        assertEquals("", sc.two);
        assertEquals("", sc.three);
    }

    /**
     * Tests adding a configurable with explicit properties.
     */
    @Test
    public void addStringConfigurable() throws IOException {
        URL cu = getClass().getResource("stringConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        Map<String,Object> m = new HashMap<String, Object>();
        for(String s : new String[] {"one", "two", "three"}) {
            m.put(s, s);
        }
        cm.addConfigurable(StringConfigurable.class, "c", m);
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("one", sc.one);
        assertEquals("two", sc.two);
        assertEquals("three", sc.three);
    }

    /**
     * Tests adding a configurable with some explicit properties and some
     * default ones.
     */
    @Test
    public void addPartialStringConfigurable() throws IOException {
        URL cu = getClass().getResource("stringConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        Map<String,Object> m = new HashMap<String, Object>();
        for(String s : new String[] {"one"}) {
            m.put(s, s);
        }
        cm.addConfigurable(StringConfigurable.class, "c", m);
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("one", sc.one);
        assertEquals("", sc.two);
        assertEquals("", sc.three);
        m.clear();
        for(String s : new String[]{"one", "three"}) {
            m.put(s, s);
        }
        cm.addConfigurable(StringConfigurable.class, "d", m);
        sc = (StringConfigurable) cm.lookup("d");
        assertEquals("one", sc.one);
        assertEquals("", sc.two);
        assertEquals("three", sc.three);
    }

    /**
     * Tests adding a configurable with an incorrect property type.
     */
    @Test(expected=PropertyException.class)
    public void addConfigurableWithBadProperty() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        Map<String,Object> m = new HashMap<String, Object>();
        m.put("s", "one");
        m.put("i", "two");
        cm.addConfigurable(BasicConfigurable.class, "c", m);
        BasicConfigurable bc = (BasicConfigurable) cm.lookup("c");
        assertEquals("one", bc.s);
        assertEquals(2, bc.i);
    }

    /**
     * Tests adding a configurable with an existing name, which should not throw
     * an exception if it the existing configurable hasn't been instantiated.  This
     * is just like overriding one configuration file with another.
     */
    @Test
    public void addAlreadyNamedStringConfigurable() throws IOException {
        URL cu = getClass().getResource("stringConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        Map<String,Object> m = new HashMap<String, Object>();
        for(String s : new String[] {"one", "two", "three"}) {
            m.put(s, s);
        }
        cm.addConfigurable(StringConfigurable.class, "a", m);
        StringConfigurable sc = (StringConfigurable) cm.lookup("a");
        assertEquals("one", sc.one);
        assertEquals("two", sc.two);
        assertEquals("three", sc.three);
    }

    /**
     * Tests adding a configurable with an existing name, which should throw
     * an exception if it the existing configurable hasn't been instantiated.
     */
    @Test(expected=IllegalArgumentException.class)
    public void addAlreadyNamedAndInstatiatedStringConfigurable() throws IOException {
        URL cu = getClass().getResource("stringConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        StringConfigurable sc = (StringConfigurable) cm.lookup("a");
        Map<String,Object> m = new HashMap<String, Object>();
        for(String s : new String[] {"one", "two", "three"}) {
            m.put(s, s);
        }
        cm.addConfigurable(StringConfigurable.class, "a", m);
        sc = (StringConfigurable) cm.lookup("a");
    }

    @Test
    public void testWriting() throws IOException {

        //
        // Add a component.
        URL cu = getClass().getResource("stringConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        Map<String, Object> m = new HashMap<String, Object>();
        for(String s : new String[]{"one", "two", "three"}) {
            m.put(s, s);
        }
        cm.addConfigurable(StringConfigurable.class, "c", m);

        //
        // Write the file.
        File f = File.createTempFile("config", ".xml");
        cm.save(f);
        cu = f.toURI().toURL();

        //
        // Re-read the file.
        cm = new ConfigurationManager(cu);
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("one", sc.one);
        assertEquals("two", sc.two);
        assertEquals("three", sc.three);
        //logger.info(String.format("f: %s", f));
        f.deleteOnExit();
    }
}
