package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.StringleConfigurable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class SubclassConfigTest {

    @BeforeClass
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @Test
    public void testStringConfigSubclass() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("subclassConfig.edn");
        StringleConfigurable scc = (StringleConfigurable) cm.lookup(
                "stringConfigSubclass");
        assertEquals("a", scc.one);
        assertEquals("b", scc.two);
        assertEquals("c", scc.three);
        assertEquals("d", scc.four);
        assertEquals("e", scc.five);
    }

}
