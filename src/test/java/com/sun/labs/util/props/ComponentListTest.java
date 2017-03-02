package com.sun.labs.util.props;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A set of tests for component lists.
 */
public class ComponentListTest {

    public ComponentListTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void componentListTest() throws IOException {
        URL cu = getClass().getResource("componentListConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        ListConfigurable lc = (ListConfigurable) cm.lookup("simple");
        List<Configurable> l = lc.getList();
        assertTrue(l.size() == 2);
        for (Configurable c : l) {
            assertNotNull(c);
        }
    }

    @Test(expected = com.sun.labs.util.props.PropertyException.class)
    public void badComponentListTest() throws IOException {
        URL cu = getClass().getResource("componentListConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        ListConfigurable lc = (ListConfigurable) cm.lookup("bad");
        for (Configurable c : lc.getList()) {
            assertNotNull(c);
        }
    }

    @Test
    public void simpleTypedTest() throws IOException {
        URL cu = getClass().getResource("componentListConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
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
        URL cu = getClass().getResource("componentListConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
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
        URL cu = getClass().getResource("componentListConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
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
}