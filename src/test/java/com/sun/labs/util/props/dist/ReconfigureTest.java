/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.util.props.dist;

import com.sun.labs.util.SimpleLabsLogFormatter;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
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
public class ReconfigureTest {

    private ConfigurationManager cm1;

    private ConfigurationManager cm2;

    private Logger log = Logger.getLogger("");

    public ReconfigureTest() {
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
    public void reconfigure() throws PropertyException, IOException {
        //
        // Register in one manager.
        URL cu = getClass().getResource("serverExceptionConfig.xml");
        cm1 = new ConfigurationManager(cu);
        ExceptionGenerator server = (ExceptionGenerator) cm1.lookup("servercomp");
        assertNotNull(server);

        //
        // Lookup in another.
        URL cu2 = getClass().getResource("clientExceptionConfig.xml");
        cm2 = new ConfigurationManager(cu2);
        ClientReconfigurable client = (ClientReconfigurable) cm2.lookup("failtest");
        Thread t = new Thread(client);
        t.setDaemon(true);
        t.start();

        //
        // Wait for the thread to finish.
        try {
            t.join();
        } catch(InterruptedException ie) {

        }

        assertTrue(client.getExCount() == server.getExCount());
        assertTrue(server.recs.size() == client.getOpCount());
    }
}
