package com.oracle.labs.mlrg.olcut.config;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 *
 */
public class SubclassConfigTest {

    @Test
    public void testStringConfigSubclass() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("subclassConfig.xml");
        StringleConfigurable scc = (StringleConfigurable) cm.lookup(
                "stringConfigSubclass");
        assertEquals("a", scc.one);
        assertEquals("b", scc.two);
        assertEquals("c", scc.three);
        assertEquals("d", scc.four);
        assertEquals("e", scc.five);
    }

}
