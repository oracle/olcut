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

package com.oracle.labs.mlrg.olcut.config.remote;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * A client class that gets a configurable object from a registrar.
 */
public class ClientConfigurable implements Configurable, Runnable {
    private static final Logger log = Logger.getLogger(ClientConfigurable.class.getName());

    @Config
    private String value = "foo";

    @Config
    private int count = 10;

    @Config
    private RegistryConfigurable comp;

    private int newPropsCalls = 0;

    private int opCount = 0;

    private boolean pause = false;

    public void run() {
        for(int i = 0; i < count; i++) {
            try {
                comp.stringOp(value);
                comp.intOp(count);
                opCount++;
                log.info(Thread.currentThread().getName() + " iteration " + i);
            } catch(RemoteException rx) {
            }
            try {
                Thread.sleep(500);
            } catch(InterruptedException ie) {

            }
        }
        log.info("Client done!");
    }

    public int getNewPropsCalls() {
        return newPropsCalls;
    }

    public int getOpCount() {
        return opCount;
    }

    @Override
    public void postConfig() throws PropertyException {
        newPropsCalls++;
        pause = false;
    }
}
