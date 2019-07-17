package com.oracle.labs.mlrg.olcut.config;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author stgreen
 */
public class StartableTest {

    @Test
    public void simpleTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("startableConfig.xml");
        StartableConfigurable sc = (StartableConfigurable) cm.lookup("startme");
        assertFalse(sc.isDone());
        sc.join();
        List<String> l = sc.getResult();
        assertEquals(5, l.size());
        for(String s : l) {
            assertEquals(s, "foo");
        }
    }
}