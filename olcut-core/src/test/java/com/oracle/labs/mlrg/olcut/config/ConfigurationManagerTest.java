package com.oracle.labs.mlrg.olcut.config;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
public class ConfigurationManagerTest {


    @Test
    public void testMultipleLoading() {
        List<String> files = new ArrayList<>();

        files.add("/com/oracle/labs/mlrg/olcut/config/timeConfig.xml");
        files.add("/com/oracle/labs/mlrg/olcut/config/stringConfig.xml");
        files.add("/com/oracle/labs/mlrg/olcut/config/typeConfig.xml");

        ConfigurationManager cm = new ConfigurationManager(files);

        Configurable a = cm.lookup("valid-time");
        assertNotNull(a);

        Configurable b = cm.lookup("ac");
        assertNotNull(b);

        Configurable c = cm.lookup("default");
        assertNotNull(c);

        assertEquals(12,cm.getComponentNames().size());
    }
}
