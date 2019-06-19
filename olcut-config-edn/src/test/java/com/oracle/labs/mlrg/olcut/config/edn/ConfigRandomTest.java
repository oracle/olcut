package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.property.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.RandomConfigurable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests the construction of {@link java.util.Random} objects from a {@link PropertySheet}.
 */
public class ConfigRandomTest {

    @BeforeClass
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
