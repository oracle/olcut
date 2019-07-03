package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.StringListConfigurable;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class StringListTest {

    @BeforeAll
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test
    public void getStrings() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringListConfig.json");
        StringListConfigurable slc = (StringListConfigurable) cm.lookup(
                "listTest");
        assertEquals("a", slc.strings.get(0));
        assertEquals("b", slc.strings.get(1));
        assertEquals("c", slc.strings.get(2));
    }
}
