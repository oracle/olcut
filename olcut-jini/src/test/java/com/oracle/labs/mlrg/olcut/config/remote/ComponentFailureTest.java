/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.labs.mlrg.olcut.config.remote;

import com.oracle.labs.mlrg.olcut.util.SimpleLabsLogFormatter;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the failure of a component by removing it from the registry.  The
 * clients should recover.
 */
public class ComponentFailureTest {

    private JiniConfigurationManager cm1;

    private JiniConfigurationManager cm2;

    private Logger log = Logger.getLogger("");

    public ComponentFailureTest() {
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
    public void tearDown() {
        if(cm1 != null) {
            cm1.shutdown();
        }
        if(cm2 != null) {
            cm2.shutdown();
        }
    }

    @Test
    public void simpleKillService() throws PropertyException, IOException {
        //
        // Register in one manager.
        URL cu = getClass().getResource("serverConfig.xml");
        cm1 = new JiniConfigurationManager(cu);
        RegistryConfigurable server = (RegistryConfigurable) cm1.lookup("servercomp");
        assertNotNull(server);

        //
        // Lookup in another.
        URL cu2 = getClass().getResource("clientConfig.xml");
        cm2 = new JiniConfigurationManager(cu2);
        ClientConfigurable client = (ClientConfigurable) cm2.lookup("failtest");
        Thread t = new Thread(client);
        t.setName("client-thread");
        t.start();

        try {
            Thread.sleep(2000);
        } catch(InterruptedException ie) {
            
        }

        //
        // Kill the server and re-register.
        cm1.shutdown();
        cm1 = new JiniConfigurationManager(cu);
        server = (RegistryConfigurable) cm1.lookup("servercomp");
        assertNotNull(server);

        //
        // Wait for the client to finish.
        try {
            t.join();
        } catch(InterruptedException ie) {
            return;
        }

        assertTrue(client.getNewPropsCalls() == 1);
    }

    @Test
    public void killService() throws PropertyException, IOException {
        //
        // Register in one manager.
        URL cu = getClass().getResource("serverConfig.xml");
        cm1 = new JiniConfigurationManager(cu);
        RegistryConfigurable server = (RegistryConfigurable) cm1.lookup("servercomp");
        assertNotNull(server);

        int sops = 0;

        //
        // Lookup in another.
        URL cu2 = getClass().getResource("clientConfig.xml");
        cm2 = new JiniConfigurationManager(cu2);
        ClientConfigurable client = (ClientConfigurable) cm2.lookup("failtest");
        Thread t = new Thread(client);
        t.setName("client-thread");
        t.start();

        //
        // Give it a second or two.
        try {
            Thread.sleep(2000);
        } catch(InterruptedException ie) {
            return;
        }

        //
        // Kill the server and re-register.
        cm1.shutdown();

        assertTrue(client.getOpCount() == ((RegistryConfigurableImpl) server).getIOPCount());
        sops = ((RegistryConfigurableImpl) server).getIOPCount();

        cm1 = new JiniConfigurationManager(cu);
        server = (RegistryConfigurable) cm1.lookup("servercomp");
        assertNotNull(server);

        try {
            t.join();
        } catch(InterruptedException ie) {
        }
        assertTrue(client.getNewPropsCalls() == 1);
        sops += ((RegistryConfigurableImpl) server).getIOPCount();
        assertTrue(client.getOpCount() == sops);
    }
}
