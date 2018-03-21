package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ArrayStringConfigurable;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.ListConfigurable;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.SimpleConfigurable;
import com.oracle.labs.mlrg.olcut.config.StringConfigurable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A set of tests for component lists.
 */
public class ComponentListTest {

    public ComponentListTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void componentListTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("componentListConfig.edn");
        ListConfigurable lc = (ListConfigurable) cm.lookup("simple");
        List<Configurable> l = lc.getList();
        assertTrue(l.size() == 2);
        for (Configurable c : l) {
            assertNotNull(c);
        }
    }

    @Test(expected = PropertyException.class)
    public void badComponentListTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("componentListConfig.edn");
        ListConfigurable lc = (ListConfigurable) cm.lookup("bad");
        for (Configurable c : lc.getList()) {
            assertNotNull(c);
        }
    }

    @Test
    public void simpleTypedTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("componentListConfig.edn");
        ListConfigurable lc = (ListConfigurable) cm.lookup("typed");
        List<Configurable> l = lc.getList();
        assertTrue(l.size() == 3);
        for (Configurable c : l) {
            assertNotNull(c);
            assertEquals(c.getClass(), StringConfigurable.class);
        }
    }

    @Test
    public void dualTypedTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("componentListConfig.edn");
        ListConfigurable lc = (ListConfigurable) cm.lookup("dualtyped");
        List<Configurable> l = lc.getList();
        assertTrue(l.size() == 5);
        int simp = 0;
        int string = 0;
        for (int i = 0; i < l.size(); i++) {
            Configurable c = l.get(i);
            assertNotNull(c);
            if(c.getClass().equals(SimpleConfigurable.class)) {
                simp++;
            } else if(c.getClass().equals(StringConfigurable.class)) {
                string++;
            } else {
                fail("Unknown class: " + c.getClass());
            }
        }

        assertTrue(simp == 2);
        assertTrue(string == 3);
    }

    @Test
    public void comboTypedTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("componentListConfig.edn");
        ListConfigurable lc = (ListConfigurable) cm.lookup("combotyped");
        List<Configurable> l = lc.getList();
        assertTrue(l.size() == 4);
        int simp = 0;
        int string = 0;
        for (int i = 0; i < l.size(); i++) {
            Configurable c = l.get(i);
            assertNotNull(c);
            if (c.getClass().equals(SimpleConfigurable.class)) {
                simp++;
            } else if (c.getClass().equals(StringConfigurable.class)) {
                string++;
            } else {
                fail("Unknown class: " + c.getClass());
            }
        }
        assertTrue(simp == 1);
        assertTrue(string == 3);
    }

    @Test
    public void stringConfigurableArrayTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("componentListConfig.edn");
        ArrayStringConfigurable lc = (ArrayStringConfigurable) cm.lookup("stringconfigurablearray");
        StringConfigurable[] l = lc.getArray();
        assertTrue(l.length == 3);
        String firstOne = l[0].one;
        assertEquals("alpha",firstOne);
        String secondOne = l[1].one;
        assertEquals("one",secondOne);
        String thirdOne = l[2].one;
        assertEquals("un",thirdOne);
    }

    @Test(expected=PropertyException.class)
    public void stringConfigurableBrokenArrayTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("componentListConfig.edn");
        ArrayStringConfigurable lc = (ArrayStringConfigurable) cm.lookup("stringconfigurablearraybroken");
        fail("Did not throw PropertyException when asking for unknown element in configurable array.");
    }
}