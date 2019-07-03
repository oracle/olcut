package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.config.property.Property;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.util.LabsLogFormatter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class ProgrammaticConfigurableTest {

    private static final Logger logger = Logger.getLogger(ProgrammaticConfigurableTest.class.getName());

    @BeforeAll
    public static void setUpClass() throws Exception {
        LabsLogFormatter.setAllLogFormatters(Level.FINER);
    }

    /**
     * Tests adding a configurable with the default properties.
     */
    @Test
    public void addDefaultStringConfigurable() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringConfig.xml");
        cm.addConfiguration(StringConfigurable.class, "c");
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
        ConfigurationManager cm = new ConfigurationManager("stringConfig.xml");
        Map<String, Property> m = new HashMap<>();
        for(String s : new String[] {"one", "two", "three"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfiguration(new ConfigurationData("c",StringConfigurable.class.getName(),m));
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
        ConfigurationManager cm = new ConfigurationManager("stringConfig.xml");
        Map<String,Property> m = new HashMap<>();
        for(String s : new String[] {"one"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfiguration(new ConfigurationData("c",StringConfigurable.class.getName(),m));
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("one", sc.one);
        assertEquals("", sc.two);
        assertEquals("", sc.three);
        m.clear();
        for(String s : new String[]{"one", "three"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfiguration(new ConfigurationData("d",StringConfigurable.class.getName(),m));
        sc = (StringConfigurable) cm.lookup("d");
        assertEquals("one", sc.one);
        assertEquals("", sc.two);
        assertEquals("three", sc.three);
    }

    /**
     * Tests adding a configurable with an incorrect property type.
     */
    @Test
    public void addConfigurableWithBadProperty() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("basicConfig.xml");
            Map<String,Property> m = new HashMap<>();
            m.put("s", new SimpleProperty("one"));
            m.put("i", new SimpleProperty("two"));
            cm.addConfiguration(new ConfigurationData("c",BasicConfigurable.class.getName(),m));
            BasicConfigurable bc = (BasicConfigurable) cm.lookup("c");
            assertEquals("one", bc.s);
            assertEquals(2, bc.i);
        });
    }

    /**
     * Tests adding a configurable with an existing name, which should not throw
     * an exception if it the existing configurable hasn't been instantiated.  This
     * is just like overriding one configuration file with another.
     */
    @Test
    public void addAlreadyNamedStringConfigurable() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringConfig.xml");
        Map<String,Property> m = new HashMap<>();
        for(String s : new String[] {"one", "two", "three"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfiguration(new ConfigurationData("a",StringConfigurable.class.getName(),m));
        StringConfigurable sc = (StringConfigurable) cm.lookup("a");
        assertEquals("one", sc.one);
        assertEquals("two", sc.two);
        assertEquals("three", sc.three);
    }

    /**
     * Tests adding a configurable with an existing name, which should throw
     * an exception if it the existing configurable hasn't been instantiated.
     */
    @Test
    public void addAlreadyNamedAndInstatiatedStringConfigurable() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("stringConfig.xml");
            StringConfigurable sc = (StringConfigurable) cm.lookup("a");
            Map<String,Property> m = new HashMap<>();
            for(String s : new String[] {"one", "two", "three"}) {
                m.put(s, new SimpleProperty(s));
            }
            cm.addConfiguration(new ConfigurationData("a",StringConfigurable.class.getName(),m));
            sc = (StringConfigurable) cm.lookup("a");
        });
    }

    @Test
    public void testWriting() throws IOException {
        //
        // Add a component.
        ConfigurationManager cm = new ConfigurationManager("stringConfig.xml");
        Map<String, Property> m = new HashMap<>();
        for(String s : new String[]{"one", "two", "three"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfiguration(new ConfigurationData("c",StringConfigurable.class.getName(),m));
        StringConfigurable c = (StringConfigurable) cm.lookup("c");

        //
        // Write the file.
        File f = File.createTempFile("config", ".xml");
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
