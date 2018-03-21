/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.StartableConfigurable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author stgreen
 */
public class StartableTest {

    public StartableTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void simpleTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("startableConfig.edn");
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