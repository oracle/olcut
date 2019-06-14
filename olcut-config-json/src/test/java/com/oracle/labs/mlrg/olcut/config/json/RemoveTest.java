package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.BasicConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.Property;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.SimpleProperty;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class RemoveTest {

    public RemoveTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInstantiatedRemove() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("basicConfig.json");
        BasicConfigurable bc = (BasicConfigurable) cm.lookup("a");
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals(bc.s, ps.getProperty("s").toString());
        assertEquals(bc.i, Integer.parseInt(ps.getProperty("i").toString()));
        assertEquals(bc.d, Double.parseDouble(ps.getProperty("d").toString()), 0.001);
        try {
            BasicConfigurable nbc = (BasicConfigurable) cm.lookup("a");
            fail("Found a removed component");
        } catch (PropertyException e) { }
    }

    @Test
    public void testUninstantiatedRemove() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("basicConfig.json");
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals("one", ps.getProperty("s").toString());
        assertEquals(2, Integer.parseInt(ps.getProperty("i").toString()));
        assertEquals(3.0, Double.parseDouble(ps.getProperty("d").toString()), 0.001);
        try{
            BasicConfigurable nbc = (BasicConfigurable) cm.lookup("a");
            fail("Found a removed component");
        } catch (PropertyException e) { }
    }

    @Test
    public void removeProgramaticallyAddedUninstantiated() throws IOException {
        ConfigurationManager cm = new ConfigurationManager();
        Map<String,Property> m = new HashMap<>();
        m.put("s", new SimpleProperty("foo"));
        m.put("i", new SimpleProperty(""+7));
        m.put("d", new SimpleProperty(""+2.71));
        cm.addConfigurable(BasicConfigurable.class, "a", m);
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals(m.get("s"), ps.getProperty("s"));
        assertEquals(Integer.parseInt(((SimpleProperty) m.get("i")).getValue()), Integer.parseInt(ps.getProperty("i").toString()));
        assertEquals(Double.parseDouble(((SimpleProperty) m.get("d")).getValue()), Double.parseDouble(ps.getProperty("d").toString()), 0.001);
        try{
            BasicConfigurable bc = (BasicConfigurable) cm.lookup("a");
            fail("Found a removed component");
        } catch (PropertyException e) { }
    }

    @Test
    public void removeProgramaticallyAddedInstantiated() throws IOException {
        ConfigurationManager cm = new ConfigurationManager();
        Map<String, Property> m = new HashMap<>();
        m.put("s", new SimpleProperty("foo"));
        m.put("i", new SimpleProperty(""+7));
        m.put("d", new SimpleProperty(""+2.71));
        cm.addConfigurable(BasicConfigurable.class, "a", m);
        BasicConfigurable bc = (BasicConfigurable) cm.lookup("a");
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals(m.get("s"), ps.getProperty("s"));
        assertEquals(Integer.parseInt(((SimpleProperty) m.get("i")).getValue()), Integer.parseInt(ps.getProperty("i").toString()));
        assertEquals(Double.parseDouble(((SimpleProperty) m.get("d")).getValue()), Double.parseDouble(ps.getProperty("d").toString()), 0.001);
    }

    @Test
    public void removeUninstantiatedWithEmbeddedComponents() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("importConfig.json");
        PropertySheet ps = cm.getPropertySheet("l1");
        assertEquals(cm.getNumConfigured(), 0);
    }
}