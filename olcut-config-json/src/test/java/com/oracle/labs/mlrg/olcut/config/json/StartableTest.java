/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.StartableConfigurable;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class StartableTest {

    public StartableTest() {
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test
    public void simpleTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("startableConfig.json");
        StartableConfigurable sc = (StartableConfigurable) cm.lookup("startme");
        assertTrue(!sc.isDone());
        sc.join();
        List<String> l = sc.getResult();
        assertTrue(l.size() == 5);
        for(String s : l) {
            assertEquals(s, "foo");
        }
    }
}