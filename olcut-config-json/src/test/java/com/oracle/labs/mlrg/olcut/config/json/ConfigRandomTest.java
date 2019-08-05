package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.RandomConfigurable;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the construction of {@link java.util.Random} objects from a {@link PropertySheet}.
 *
 * This has been deprecated in OLCUT v5, and will be removed
 */
public class ConfigRandomTest {

    @BeforeAll
    public static void setup() {
        Logger logger = Logger.getLogger(PropertySheet.class.getName());
        logger.setLevel(Level.SEVERE);
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test
    public void configRandom() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("randomConfig.json");
        RandomConfigurable r = (RandomConfigurable) cm.lookup("random");
        int first = r.one.nextInt();
        int second = r.two.nextInt();
        assertEquals(-1157793070,first);
        assertEquals(-1150867590,second);
    }
}
