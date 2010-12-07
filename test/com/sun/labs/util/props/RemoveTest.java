/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

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
        BasicConfigurable nbc = (BasicConfigurable) cm.lookup("a");
        assertNull(nbc);
    }

    @Test
    public void testUninstantiatedRemove() throws IOException {
        URL cu = getClass().getResource("basicConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        PropertySheet ps = cm.removeConfigurable("a");
        assertNotNull(ps);
        BasicConfigurable nbc = (BasicConfigurable) cm.lookup("a");
        assertNull(nbc);
    }

}