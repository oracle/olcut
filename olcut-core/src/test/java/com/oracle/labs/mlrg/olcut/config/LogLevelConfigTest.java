/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oracle.labs.mlrg.olcut.config;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stgreen
 */
public class LogLevelConfigTest {

    public LogLevelConfigTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Ignore
    @Test
    public void testLogLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simplewarn");
    }

    @Ignore
    @Test
    public void testGlobalLogLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simpleglobal");
        assertEquals(sc.getLogLevel(), Level.INFO);
    }

    @Ignore
    @Test
    public void testWarnLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simplewarn");
        assertEquals(sc.getLogLevel(), Level.WARNING);
    }

    @Ignore
    @Test
    public void testSevereLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simplesevere");
        assertEquals(sc.getLogLevel(), Level.SEVERE);
    }

    @Ignore
    @Test(expected=PropertyException.class)
    public void testBadLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simplebad");
    }

    @Ignore
    @Test(expected=PropertyException.class)
    public void testBadGlobalLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig2.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simpleglobal");
    }

}