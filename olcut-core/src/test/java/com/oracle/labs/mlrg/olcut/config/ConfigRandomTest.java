package com.oracle.labs.mlrg.olcut.config;

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
    }

    @Test
    public void configRandom() {
        ConfigurationManager cm = new ConfigurationManager("randomConfig.xml");
        RandomConfigurable r = (RandomConfigurable) cm.lookup("random");
        int first = r.one.nextInt();
        int second = r.two.nextInt();
        assertEquals(-1157793070,first);
        assertEquals(-1150867590,second);
    }
}
