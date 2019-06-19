package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.RandomConfigurable;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Tests the construction of {@link java.util.Random} objects from a {@link PropertySheet}.
 */
public class ConfigRandomTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    public ConfigRandomTest() { }

    @Test
    public void configRandom() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("randomConfig.edn");
        RandomConfigurable r = (RandomConfigurable) cm.lookup("random");
        int first = r.one.nextInt();
        int second = r.two.nextInt();
        assertEquals(-1157793070,first);
        assertEquals(-1150867590,second);
    }
}
