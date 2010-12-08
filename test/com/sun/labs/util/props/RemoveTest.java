/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.net.URL;
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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInstantiatedRemove() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        BasicConfigurable bc = (BasicConfigurable) cm.lookup("a");
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals(bc.s, ps.getString("s"));
        assertEquals(bc.i, ps.getInt("i"));
        assertEquals(bc.d, ps.getDouble("d"), 0.001);
        BasicConfigurable nbc = (BasicConfigurable) cm.lookup("a");
        assertNull(nbc);
    }

    @Test
    public void testUninstantiatedRemove() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals("one", ps.getString("s"));
        assertEquals(2, ps.getInt("i"));
        assertEquals(3.0, ps.getDouble("d"), 0.001);
        BasicConfigurable nbc = (BasicConfigurable) cm.lookup("a");
        assertNull(nbc);
    }

    @Test
    public void removeProgramaticallyAddedUninstantiated() {
        ConfigurationManager cm = new ConfigurationManager();
        Map<String,Object> m = new HashMap<String,Object>();
        m.put("s", "foo");
        m.put("i", 7);
        m.put("d", 2.71);
        cm.addConfigurable(BasicConfigurable.class, "a", m);
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals(m.get("s"), ps.getString("s"));
        assertEquals(((Integer) m.get("i")).intValue(), ps.getInt("i"));
        assertEquals((Double) m.get("d"), ps.getDouble("d"), 0.001);
        BasicConfigurable bc = (BasicConfigurable) cm.lookup("a");
        assertNull(bc);
    }

    @Test
    public void removeProgramaticallyAddedInstantiated() {
        ConfigurationManager cm = new ConfigurationManager();
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("s", "foo");
        m.put("i", 7);
        m.put("d", 2.71);
        cm.addConfigurable(BasicConfigurable.class, "a", m);
        BasicConfigurable bc = (BasicConfigurable) cm.lookup("a");
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        assertEquals(m.get("s"), ps.getString("s"));
        assertEquals(((Integer) m.get("i")).intValue(), ps.getInt("i"));
        assertEquals((Double) m.get("d"), ps.getDouble("d"), 0.001);
    }

    @Test
    public void removeUninstantiatedWithEmbeddedComponents() throws IOException {
        URL cu = getClass().getResource("importConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        PropertySheet ps = cm.getPropertySheet("l1");
        assertEquals(cm.getNumConfigured(), 0);
    }
}