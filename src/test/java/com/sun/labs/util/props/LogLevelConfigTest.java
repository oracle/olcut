/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
    
    @Test
    public void testLogLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simplewarn");
    }

    @Test
    public void testGlobalLogLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simpleglobal");
        assertEquals(sc.getLogLevel(), Level.INFO);
    }

    @Test
    public void testWarnLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simplewarn");
        assertEquals(sc.getLogLevel(), Level.WARNING);
    }

    @Test
    public void testSevereLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simplesevere");
        assertEquals(sc.getLogLevel(), Level.SEVERE);
    }

    @Test(expected=com.sun.labs.util.props.PropertyException.class)
    public void testBadLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simplebad");
    }

    @Test(expected=com.sun.labs.util.props.PropertyException.class)
    public void testBadGlobalLevel() throws IOException {
        URL cu = getClass().getResource("logLevelConfig2.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        SimpleConfigurable sc = (SimpleConfigurable) cm.lookup("simpleglobal");
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

}