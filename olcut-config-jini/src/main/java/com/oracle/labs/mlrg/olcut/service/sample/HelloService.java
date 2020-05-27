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

import com.oracle.labs.mlrg.olcut.config.Configurable;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A sample service.
 */
public interface HelloService extends Configurable, Remote {
    
    /**
     * Says hello
     * @return the string "hello"
     * @throws RemoteException If an error occurred in the remote JVM.
     */
    public String hello() throws RemoteException;
    
    /**
     * Echoes the provided string.
     * @param s the string to echo
     * @return the same string
     * @throws RemoteException If an error occurred in the remote JVM.
     */
    public String echo(String s) throws RemoteException;
    
    /**
     * Adds two doubles together in a very friendly way.
     * 
     * @param d1 the first double
     * @param d2 the second double
     * @return d1 + d2
     * @throws RemoteException If an error occurred in the remote JVM.
     */
    public double add(double d1, double d2) throws RemoteException;
}
