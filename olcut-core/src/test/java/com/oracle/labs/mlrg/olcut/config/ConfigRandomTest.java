package com.oracle.labs.mlrg.olcut.config;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests the construction of {@link java.util.Random} objects from a {@link PropertySheet}.
 */
public class ConfigRandomTest {
    
    public ConfigRandomTest() { }

    @Test
    public void configRandom() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("randomConfig.xml");
        RandomConfigurable r = (RandomConfigurable) cm.lookup("random");
        int first = r.one.nextInt();
        int second = r.two.nextInt();
        assertEquals(-1157793070,first);
        assertEquals(-1150867590,second);
    }
}
