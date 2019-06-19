package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.BasicConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.Property;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.SimpleProperty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class RemoveTest {

    public RemoveTest() {
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @Test
    public void testInstantiatedRemove() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("basicConfig.edn");
        BasicConfigurable bc = (BasicConfigurable) cm.lookup("a");
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals(bc.s, ((SimpleProperty) ps.getRaw("s")).getValue());
        assertEquals(bc.i, Integer.parseInt(ps.getRaw("i").toString()));
        assertEquals(bc.d, Double.parseDouble(ps.getRaw("d").toString()), 0.001);
        try {
            BasicConfigurable nbc = (BasicConfigurable) cm.lookup("a");
            fail("Found a removed component");
        } catch (PropertyException e) { }
    }

    @Test
    public void testUninstantiatedRemove() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("basicConfig.edn");
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals("one", ((SimpleProperty) ps.getRaw("s")).getValue());
        assertEquals(2, Integer.parseInt(ps.getRaw("i").toString()));
        assertEquals(3.0, Double.parseDouble(ps.getRaw("d").toString()), 0.001);
        try{
            BasicConfigurable nbc = (BasicConfigurable) cm.lookup("a");
            fail("Found a removed component");
        } catch (PropertyException e) { }
    }

    @Test
    public void removeProgramaticallyAddedUninstantiated() throws IOException {
        ConfigurationManager cm = new ConfigurationManager();
        Map<String, Property> m = new HashMap<>();
        m.put("s", new SimpleProperty("foo"));
        m.put("i", new SimpleProperty(Integer.toString(7)));
        m.put("d", new SimpleProperty(Double.toString(2.71)));
        cm.addConfigurable(BasicConfigurable.class, "a", m);
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals(m.get("s"), ps.getRaw("s"));
        assertEquals(Integer.parseInt(((SimpleProperty) m.get("i")).getValue()), Integer.parseInt(ps.getRaw("i").toString()));
        assertEquals(Double.parseDouble(((SimpleProperty) m.get("d")).getValue()), Double.parseDouble(ps.getRaw("d").toString()), 0.001);
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
        m.put("i", new SimpleProperty(Integer.toString(7)));
        m.put("d", new SimpleProperty(Double.toString(2.71)));
        cm.addConfigurable(BasicConfigurable.class, "a", m);
        BasicConfigurable bc = (BasicConfigurable) cm.lookup("a");
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals(m.get("s"), ps.getRaw("s"));
        assertEquals(Integer.parseInt(((SimpleProperty) m.get("i")).getValue()), Integer.parseInt(ps.getRaw("i").toString()));
        assertEquals(Double.parseDouble(((SimpleProperty) m.get("d")).getValue()), Double.parseDouble(ps.getRaw("d").toString()), 0.001);
    }

    @Test
    public void removeUninstantiatedWithEmbeddedComponents() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("importConfig.edn");
        PropertySheet ps = cm.getPropertySheet("l1");
        assertEquals(cm.getNumConfigured(), 0);
    }
}