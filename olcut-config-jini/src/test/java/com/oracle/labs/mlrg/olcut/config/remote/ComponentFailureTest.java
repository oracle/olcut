/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.labs.mlrg.olcut.config.remote;

import com.oracle.labs.mlrg.olcut.util.SimpleLabsLogFormatter;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


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

    @BeforeAll
    public static void setUpClass() throws Exception {
        Logger l = Logger.getLogger("");
        for(Handler h : l.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }
    }

    @BeforeEach
    public void setUp() {
        cm1 = null;
        cm2 = null;
    }

    @AfterEach
    public void tearDown() {
        if(cm1 != null) {
            cm1.close();
        }
        if(cm2 != null) {
            cm2.close();
        }
    }

    @Test
    public void simpleKillService() throws PropertyException, IOException {
        //
        // Register in one manager.
        cm1 = new JiniConfigurationManager("/com/oracle/labs/mlrg/olcut/config/remote/serverConfig.xml");
        RegistryConfigurable server = (RegistryConfigurable) cm1.lookup("servercomp");
        assertNotNull(server);

        //
        // Lookup in another.
        cm2 = new JiniConfigurationManager("/com/oracle/labs/mlrg/olcut/config/remote/clientConfig.xml");
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
        cm1.close();
        cm1 = new JiniConfigurationManager("/com/oracle/labs/mlrg/olcut/config/remote/serverConfig.xml");
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
        cm1 = new JiniConfigurationManager("/com/oracle/labs/mlrg/olcut/config/remote/serverConfig.xml");
        RegistryConfigurable server = (RegistryConfigurable) cm1.lookup("servercomp");
        assertNotNull(server);

        int sops = 0;

        //
        // Lookup in another.
        cm2 = new JiniConfigurationManager("/com/oracle/labs/mlrg/olcut/config/remote/clientConfig.xml");
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
        cm1.close();

        assertTrue(client.getOpCount() == ((RegistryConfigurableImpl) server).getIOPCount());
        sops = ((RegistryConfigurableImpl) server).getIOPCount();

        cm1 = new JiniConfigurationManager("/com/oracle/labs/mlrg/olcut/config/remote/serverConfig.xml");
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
