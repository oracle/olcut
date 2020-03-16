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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the interface for a remote component.  This component
 * will generate a remote exception every three operations.
 */
public class ExceptionGenerator implements RegistryConfigurable, Configurable {
    
    @Config
    private String prefix = "prefix";
    
    @Config
    private int incr = 1;
    
    public List<String> recs = new ArrayList<String>();
    
    private int sopCount = 0;
    
    private int iopCount = 0;
    
    private int exCount = 0;
    
    private int rawCount = 0;
    
    public int getIOPCount() {
        return iopCount;
    }
    
    public int getExCount() {
        return exCount;
    }

    public String stringOp(String s) throws RemoteException {
        rawCount++;
        if(rawCount % 3 == 2) {
            exCount++;
            throw new RemoteException("It's an exception!");
        }
        recs.add(s);
        sopCount++;
        return prefix + s;
    }

    public int intOp(int x) throws RemoteException {
        iopCount++;
        return x + incr;
    }
}
