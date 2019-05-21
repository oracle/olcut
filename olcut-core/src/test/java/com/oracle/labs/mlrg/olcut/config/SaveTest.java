package com.oracle.labs.mlrg.olcut.config;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
        ConfigurationManager cm1 = new ConfigurationManager("genericConfig.xml");
        SetConfig s1 = (SetConfig) cm1.lookup("correctSetConfig");
        cm1.save(f, true);
        assertEquals(3, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        SetConfig s2 = (SetConfig) cm2.lookup("correctSetConfig");
        assertEquals(s1, s2);
        ListConfig l1 = (ListConfig) cm1.lookup("correctListConfig");
        ListConfig l2 = (ListConfig) cm2.lookup("correctListConfig");
        assertEquals(l1, l2);
    }

    @Test
    public void saveAllWithNoInstantiationGeneric() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("genericConfig.xml");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        SetConfig s1 = (SetConfig) cm1.lookup("correctSetConfig");
        SetConfig s2 = (SetConfig) cm2.lookup("correctSetConfig");
        assertEquals(s1, s2);
        ListConfig l1 = (ListConfig) cm1.lookup("correctListConfig");
        ListConfig l2 = (ListConfig) cm2.lookup("correctListConfig");
        assertEquals(l1, l2);
    }

    @Test
    public void saveAllWithInstantiation() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.xml");
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        cm1.save(f, true);
        assertEquals(1, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertEquals(bc1, bc2);
        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }

    @Test
    public void saveAllWithNoInstantiation() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.xml");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertEquals(bc1, bc2);
        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }
    
    @Test
    public void saveAllWithInstantiationAndAddition() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.xml");
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        Map<String, Property> m = new HashMap<>();
        m.put("s", new SimpleProperty("foo"));
        m.put("i", new SimpleProperty(""+7));
        m.put("d", new SimpleProperty(""+2.71));
        cm1.addConfigurable(BasicConfigurable.class, "c", m);
        cm1.save(f, true);
        assertEquals(1, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
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
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.xml");
        Map<String, Property> m = new HashMap<>();
        m.put("s", new SimpleProperty("foo"));
        m.put("i", new SimpleProperty(""+7));
        m.put("d", new SimpleProperty(""+2.71));
        cm1.addConfigurable(BasicConfigurable.class, "c", m);
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());
        
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
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
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.xml");
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        cm1.removeConfigurable("a");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        BasicConfigurable bc2;
        try {
            bc2 = (BasicConfigurable) cm2.lookup("a");
            fail("Found removed component");
        } catch (PropertyException e) {}

        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }

    @Test
    public void saveAllWithoutInstantiationAndRemoval() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.xml");
        cm1.removeConfigurable("a");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumConfigured());

        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        BasicConfigurable bc2;
        try{
            bc2 = (BasicConfigurable) cm2.lookup("a");
            fail("Found a removed component");
        } catch (PropertyException e) { }

        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }

    @Test
    public void removeProgramaticallyAddedUninstantiated() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.xml");
        Map<String, Property> m = new HashMap<>();
        m.put("s", new SimpleProperty("foo"));
        m.put("i", new SimpleProperty(""+7));
        m.put("d", new SimpleProperty(""+2.71));
        cm1.addConfigurable(BasicConfigurable.class, "c", m);
        PropertySheet ps = cm1.removeConfigurable("c");
        assertNotNull(ps);
        assertEquals(m.get("s"), ps.getRaw("s"));
        assertEquals(Integer.parseInt(((SimpleProperty) m.get("i")).getValue()), Integer.parseInt(ps.getRaw("i").toString()));
        assertEquals(Double.parseDouble(((SimpleProperty) m.get("d")).getValue()), Double.parseDouble(ps.getRaw("d").toString()), 0.001);
        cm1.save(f, false);
        try{
            BasicConfigurable bc = (BasicConfigurable) cm1.lookup("c");
            fail("Found a removed component");
        } catch (PropertyException e) { }

        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        try{
            BasicConfigurable bc = (BasicConfigurable) cm2.lookup("c");
            fail("Found a removed component");
        } catch (PropertyException e) { }
    }

    @Test
    public void addNewConfigProgramatically() throws IOException {
        StringConfigurable sc = new StringConfigurable("foo", "bar", "quux");

        ConfigurationManager cm = new ConfigurationManager();
        cm.importConfigurable(sc, "testStringConfig");
        cm.save(f, false);

        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        StringConfigurable sc2 = (StringConfigurable) cm2.lookup("testStringConfig");
        assertEquals(sc,sc2);
    }

    @Test
    public void loadNastyStrings() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("nastyStringConfig.xml");
        StringConfigurable sc = (StringConfigurable) cm.lookup(
                "nastyStringTest");
        assertEquals("([^a-z0-9_!#$%&*@＠]|^|RT:?)(@＠+)([a-z0-9_]{1,20})(/[a-z][a-z0-9_\\\\-]{0,24})?", sc.one);
        assertEquals("@＠", sc.two);
        assertEquals("&&", sc.three);
    }

    @Test
    public void saveNastyStrings() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager();

        StringConfigurable sc1 = new StringConfigurable("([^a-z0-9_!#$%&*@＠]|^|RT:?)(@＠+)([a-z0-9_]{1,20})(/[a-z][a-z0-9_\\\\-]{0,24})?","@＠","&&");

        cm1.importConfigurable(sc1,"nastyString");
        cm1.save(f);

        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        StringConfigurable sc2 = (StringConfigurable) cm2.lookup("nastyString");
        assertEquals(sc1,sc2);
    }
}