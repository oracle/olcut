/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oracle.labs.mlrg.olcut.config;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stgreen
 */
public class StartableTest {

    public StartableTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void simpleTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("startableConfig.xml");
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