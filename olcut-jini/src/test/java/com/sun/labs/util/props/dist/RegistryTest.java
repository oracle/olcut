/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.util.props.dist;

import com.sun.labs.util.SimpleLabsLogFormatter;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stgreen
 */
public class RegistryTest {

    private JiniConfigurationManager cm1;

    private JiniConfigurationManager cm2;

    public RegistryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger l = Logger.getLogger("");
        for(Handler h : l.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        cm1 = null;
        cm2 = null;
    }

    @After
    public void tearDown() throws Exception {
        if(cm1 != null) {
            cm1.shutdown();
        }
        if(cm2 != null) {
            cm2.shutdown();
        }
    }

    @Test
    public void testRegister() throws IOException {
        URL cu = getClass().getResource("serverConfig.xml");
        cm1 = new JiniConfigurationManager(cu);
        RegistryConfigurable rc = (RegistryConfigurable) cm1.lookup("servercomp");
        assertNotNull(rc);
    }

    @Test
    public void testRegisterWithEntries() throws IOException {
        URL cu = getClass().getResource("serverConfig.xml");
        cm1 = new JiniConfigurationManager(cu);
        RegistryConfigurable rc = (RegistryConfigurable) cm1.lookup("servercompWithEntries");
        assertNotNull(rc);
    }

    @Test
    public void testSpecificRegister() throws IOException {
        URL cu = getClass().getResource("specificRegConfig.xml");
        cm1 = new JiniConfigurationManager(cu);
        RegistryConfigurable rc = (RegistryConfigurable) cm1.lookup("servercomp");
        cm1.shutdown();
        assertNotNull(rc);
    }

    @Test
    public void testRegisterAndLookup() throws IOException {

        //
        // Register in one manager.
        URL cu = getClass().getResource("serverConfig.xml");
        cm1 = new JiniConfigurationManager(cu);
        RegistryConfigurable rc1 = (RegistryConfigurable) cm1.lookup("servercomp");
        assertNotNull(rc1);

        //
        // Lookup in another.
        URL cu2 = getClass().getResource("clientConfig.xml");
        cm2 = new JiniConfigurationManager(cu2);
        RegistryConfigurable rc2 = (RegistryConfigurable) cm2.lookup("servercomp");

        assertEquals(rc2.stringOp("test"), "Received: test");

        //
        // Make sure that the method ran in the first object!
        assertTrue(((RegistryConfigurableImpl) rc1).recs.size() == 1);
        assertEquals(((RegistryConfigurableImpl) rc1).recs.get(0), "test");
    }

    @Test
    public void testRegisterAndLookupWithEntries() throws IOException {

        //
        // Register in one manager.
        URL cu = getClass().getResource("serverConfig.xml");
        cm1 = new JiniConfigurationManager(cu);
        RegistryConfigurable rc1 = (RegistryConfigurable) cm1.lookup("servercompWithEntries");
        assertNotNull(rc1);

        //
        // Lookup in another.
        URL cu2 = getClass().getResource("clientConfig.xml");
        cm2 = new JiniConfigurationManager(cu2);
        RegistryConfigurable rc2 = (RegistryConfigurable) cm2.lookup("servercompWithEntries");

        assertEquals(rc2.stringOp("test"), "Received: test");

        //
        // Make sure that the method ran in the first object!
        assertTrue(((RegistryConfigurableImpl) rc1).recs.size() == 1);
        assertEquals(((RegistryConfigurableImpl) rc1).recs.get(0), "test");
    }

    @Test
    public void testRegisterAndLookupWithPartialMatchingEntries() throws IOException {

        //
        // Register in one manager.
        URL cu = getClass().getResource("serverConfig.xml");
        cm1 = new JiniConfigurationManager(cu);
        RegistryConfigurable rc1 = (RegistryConfigurable) cm1.lookup("servercompWithEntries");
        assertNotNull(rc1);

        //
        // Lookup in another.
        URL cu2 = getClass().getResource("clientConfig.xml");
        cm2 = new JiniConfigurationManager(cu2);
        RegistryConfigurable rc2 = (RegistryConfigurable) cm2.lookup("servercompWithPartialMatchingEntries");
        assertNotNull(rc2);
        assertEquals(rc2.stringOp("test"), "Received: test");

        //
        // Make sure that the method ran in the first object!
        assertTrue(((RegistryConfigurableImpl) rc1).recs.size() == 1);
        assertEquals(((RegistryConfigurableImpl) rc1).recs.get(0), "test");
    }

    @Test
    public void testRegisterAndLookupWithNonMatchingEntries() throws IOException {

        //
        // Register in one manager.
        URL cu = getClass().getResource("serverConfig.xml");
        cm1 = new JiniConfigurationManager(cu);
        RegistryConfigurable rc1 = (RegistryConfigurable) cm1.lookup("servercompWithEntries");
        assertNotNull(rc1);

        //
        // Lookup in another.
        URL cu2 = getClass().getResource("clientConfig.xml");
        cm2 = new JiniConfigurationManager(cu2);
        RegistryConfigurable rc2 = (RegistryConfigurable) cm2.lookup("servercompWithNonMatchingEntries");
       
        //
        // The match should have failed, due to the non-matching entries.      
        assertNull(rc2);

   }

}
