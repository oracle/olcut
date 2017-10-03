package com.sun.labs.util.props;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class SubclassConfigTest {

    @Test
    public void testStringConfigSubclass() throws IOException {
        URL cu = getClass().getResource("subclassConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        StringleConfigurable scc = (StringleConfigurable) cm.lookup(
                "stringConfigSubclass");
        assertEquals("a", scc.one);
        assertEquals("b", scc.two);
        assertEquals("c", scc.three);
        assertEquals("d", scc.four);
        assertEquals("e", scc.five);
    }

}
