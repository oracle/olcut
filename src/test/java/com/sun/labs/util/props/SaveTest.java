package com.sun.labs.util.props;

import java.util.HashMap;
import java.util.Map;
import java.net.URL;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class SaveTest {

    public SaveTest() {
    }
    File f;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws IOException {
        f = File.createTempFile("config", ".xml");
//        f.deleteOnExit();
    }

    @After
    public void tearDown() {
//        f.delete();
    }

    @Test
    public void saveAllWithInstantiationGeneric() throws IOException {
        URL cu = getClass().getResource("genericConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        SetConfig s1 = (SetConfig) cm1.lookup("correctSetConfig");
        cm1.save(f, true);
        assertEquals(3, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        SetConfig s2 = (SetConfig) cm2.lookup("correctSetConfig");
        assertEquals(s1, s2);
        ListConfig l1 = (ListConfig) cm1.lookup("correctListConfig");
        ListConfig l2 = (ListConfig) cm2.lookup("correctListConfig");
        assertEquals(l1, l2);
    }

    @Test
    public void saveAllWithNoInstantiationGeneric() throws IOException {
        URL cu = getClass().getResource("genericConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        SetConfig s1 = (SetConfig) cm1.lookup("correctSetConfig");
        SetConfig s2 = (SetConfig) cm2.lookup("correctSetConfig");
        assertEquals(s1, s2);
        ListConfig l1 = (ListConfig) cm1.lookup("correctListConfig");
        ListConfig l2 = (ListConfig) cm2.lookup("correctListConfig");
        assertEquals(l1, l2);
    }

    @Test
    public void saveAllWithInstantiation() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        cm1.save(f, true);
        assertEquals(1, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertEquals(bc1, bc2);
        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }

    @Test
    public void saveAllWithNoInstantiation() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertEquals(bc1, bc2);
        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }
    
    @Test
    public void saveAllWithInstantiationAndAddition() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("s", "foo");
        m.put("i", 7);
        m.put("d", 2.71);
        cm1.addConfigurable(BasicConfigurable.class, "c", m);
        cm1.save(f, true);
        assertEquals(1, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertEquals(bc1, bc2);
        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
        
        bc2 = (BasicConfigurable) cm2.lookup("c");
        assertEquals("foo", bc2.s);
        assertEquals(7, bc2.i);
        assertEquals(2.71, bc2.d, 0.01);
    }

    @Test
    public void saveAllWithoutInstantiationAndAddition() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("s", "foo");
        m.put("i", 7);
        m.put("d", 2.71);
        cm1.addConfigurable(BasicConfigurable.class, "c", m);
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());
        
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertEquals(bc1, bc2);
        
        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);

        bc2 = (BasicConfigurable) cm2.lookup("c");
        assertEquals("foo", bc2.s);
        assertEquals(7, bc2.i);
        assertEquals(2.71, bc2.d, 0.01);
    }

    @Test
    public void saveAllWithInstantiationAndRemoval() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        cm1.removeConfigurable("a");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertNull(bc2);

        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }

    @Test
    public void saveAllWithoutInstantiationAndRemoval() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        cm1.removeConfigurable("a");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());

        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertNull(bc2);

        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }

    @Test
    public void removeProgramaticallyAddedUninstantiated() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm1 = new ConfigurationManager(cu);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("s", "foo");
        m.put("i", 7);
        m.put("d", 2.71);
        cm1.addConfigurable(BasicConfigurable.class, "c", m);
        PropertySheet ps = cm1.removeConfigurable("c");
        assertNotNull(ps);
        assertEquals(m.get("s"), ps.getRaw("s"));
        assertEquals(((Integer) m.get("i")).intValue(), Integer.parseInt(ps.getRaw("i").toString()));
        assertEquals((Double) m.get("d"), Double.parseDouble(ps.getRaw("d").toString()), 0.001);
        cm1.save(f, false);
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("c");
        assertNull(bc1);
        
        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("c");
        assertNull(bc2);
    }

    @Test
    public void addNewConfigProgramatically() throws IOException {
        StringConfigurable sc = new StringConfigurable("foo", "bar", "quux");

        ConfigurationManager cm = new ConfigurationManager();
        cm.importConfigurable(sc, "testStringConfig");
        cm.save(f, false);

        ConfigurationManager cm2 = new ConfigurationManager(f.toURI().toURL());
        StringConfigurable sc2 = (StringConfigurable) cm2.lookup("testStringConfig");
        assertEquals(sc,sc2);
    }
}