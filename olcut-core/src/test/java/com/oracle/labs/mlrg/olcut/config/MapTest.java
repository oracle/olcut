package com.oracle.labs.mlrg.olcut.config;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Tests the extraction of {@link java.util.Map} objects from a {@link PropertySheet}.
 */

public class MapTest {
    public MapTest() { }

    @Test
    public void mapTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("mapConfig.xml");
        MapConfigurable m = (MapConfigurable) cm.lookup("mapTest");
        Map<String,String> map = m.map;
        assertEquals("stuff",map.get("things"));
        assertEquals("quux",map.get("foo"));
        assertNull(map.get("bar"));
    }
}
