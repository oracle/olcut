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

package com.oracle.labs.mlrg.olcut.service.sample;

import com.oracle.labs.mlrg.olcut.util.LabsLogFormatter;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;

import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A client for the hello service.
 */
public class HelloClient {
    private static final Logger logger = Logger.getLogger(HelloClient.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LabsLogFormatter.setAllLogFormatters();

        //
        // Read the configuration file.
        String configFileName = args[0];
        ConfigurationManager cm = new ConfigurationManager(configFileName);

        //
        // Get all of the registered hello services. Note that we use the interface
        // here and not the implementation. We need to do this because we'll only
        // get a stub that implements the implementation.
        List<HelloService> hellos = cm.lookupAll(HelloService.class, null);
        System.out.format("Got %d hello services%n", hellos.size());
        for(HelloService hello : hellos) {
            try {
                System.out.format("%s says %s%n", hello, hello.hello());
            } catch (RemoteException ex) {
                logger.log(Level.SEVERE, String.format("Error helloing %s", hello), ex);
            }
        }
    }
    
}
