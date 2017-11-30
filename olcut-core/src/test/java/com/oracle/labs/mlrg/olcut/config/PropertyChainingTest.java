package com.oracle.labs.mlrg.olcut.config;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the chain loading of XML files in a configuration.
 */
public class PropertyChainingTest {

    public PropertyChainingTest() { }

    @BeforeClass
    public static void setUpClass() throws Exception { }

    @AfterClass
    public static void tearDownClass() throws Exception { }
    
    @Test
    public void chainLoading() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("propertyChainingConfigA.xml");
        String stringA = cm.getGlobalProperty("stringA");
        String stringB = cm.getGlobalProperty("stringB");
        String stringC = cm.getGlobalProperty("stringC");
        StringConfigurable sca = (StringConfigurable) cm.lookup("configA");
        StringConfigurable scb = (StringConfigurable) cm.lookup("configB");
        StringConfigurable scc = (StringConfigurable) cm.lookup("configC");
        assertEquals(stringA,"HK-47");
        assertEquals(stringB,"BB-8");
        assertEquals(stringC,"C3P0");
        assertEquals(sca.one,"fileA");
        assertEquals(scb.one,"fileB");
        assertEquals(scc.one,"fileC");
    }

    @Test
    public void overlay() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("propertyChainingConfigA.xml");
        String globalA = cm.getGlobalProperty("a");
        String globalB = cm.getGlobalProperty("b");
        String globalC = cm.getGlobalProperty("c");
        StringConfigurable sca = (StringConfigurable) cm.lookup("configA");
        StringConfigurable scb = (StringConfigurable) cm.lookup("configB");
        StringConfigurable scc = (StringConfigurable) cm.lookup("configC");
        assertEquals("angry",globalA);
        assertEquals(globalB,"bus");
        assertEquals(globalC,"closing");
        assertEquals(sca.two,"bus");
        assertEquals(scb.two,"bus");
        assertEquals(scc.two,"bus");
        assertEquals(sca.three,"closing");
        assertEquals(scb.three,"closing");
        assertEquals(scc.three,"closing");
    }
    
}
