/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.BasicConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.Property;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.SimpleProperty;
import com.oracle.labs.mlrg.olcut.config.StringConfigurable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.Assert.assertEquals;

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
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
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
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
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
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
        Map<String, Property> m = new HashMap<>();
        for(String s : new String[] {"one", "two", "three"}) {
            m.put(s, new SimpleProperty(s));
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
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
        Map<String,Property> m = new HashMap<>();
        for(String s : new String[] {"one"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfigurable(StringConfigurable.class, "c", m);
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("one", sc.one);
        assertEquals("", sc.two);
        assertEquals("", sc.three);
        m.clear();
        for(String s : new String[]{"one", "three"}) {
            m.put(s, new SimpleProperty(s));
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
        ConfigurationManager cm = new ConfigurationManager("basicConfig.edn");
        Map<String,Property> m = new HashMap<>();
        m.put("s", new SimpleProperty("one"));
        m.put("i", new SimpleProperty("two"));
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
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
        Map<String,Property> m = new HashMap<>();
        for(String s : new String[] {"one", "two", "three"}) {
            m.put(s, new SimpleProperty(s));
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
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
        StringConfigurable sc = (StringConfigurable) cm.lookup("a");
        Map<String,Property> m = new HashMap<>();
        for(String s : new String[] {"one", "two", "three"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfigurable(StringConfigurable.class, "a", m);
        sc = (StringConfigurable) cm.lookup("a");
    }

    @Test
    public void testWriting() throws IOException {

        //
        // Add a component.
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
        Map<String, Property> m = new HashMap<>();
        for(String s : new String[]{"one", "two", "three"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfigurable(StringConfigurable.class, "c", m);

        //
        // Write the file.
        File f = File.createTempFile("config", ".edn");
        cm.save(f);

        //
        // Re-read the file.
        cm = new ConfigurationManager(replaceBackSlashes(f.toString()));
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("one", sc.one);
        assertEquals("two", sc.two);
        assertEquals("three", sc.three);
        //logger.info(String.format("f: %s", f));
        f.deleteOnExit();
    }
}
