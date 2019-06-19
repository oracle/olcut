package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.StringleConfigurable;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 *
 */
public class SubclassConfigTest {

    @BeforeAll
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test
    public void testStringConfigSubclass() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("subclassConfig.json");
        StringleConfigurable scc = (StringleConfigurable) cm.lookup(
                "stringConfigSubclass");
        assertEquals("a", scc.one);
        assertEquals("b", scc.two);
        assertEquals("c", scc.three);
        assertEquals("d", scc.four);
        assertEquals("e", scc.five);
    }

}
