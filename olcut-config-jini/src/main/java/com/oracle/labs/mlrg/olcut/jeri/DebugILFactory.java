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

package com.oracle.labs.mlrg.olcut.jeri;

import java.lang.reflect.InvocationHandler;
import java.rmi.Remote;
import java.rmi.server.ExportException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import net.jini.core.constraint.MethodConstraints;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.InvocationDispatcher;
import net.jini.jeri.ObjectEndpoint;
import net.jini.jeri.ServerCapabilities;

/**
 *
 * @author stgreen
 */
@Deprecated
public class DebugILFactory extends BasicILFactory {
    
    private static final Logger logger = Logger.getLogger(DebugILFactory.class.getName());
    
    private MethodConstraints serverConstraints = null;
    private Class permissionClass = null;
    
    private Map<String,Integer> reportMap;


    public DebugILFactory() {
        super();
    }

    public DebugILFactory(MethodConstraints serverConstraints, Class permissionClass) {
        super(serverConstraints, permissionClass);
        this.serverConstraints = serverConstraints;
        this.permissionClass = permissionClass;
    }

    public DebugILFactory(MethodConstraints serverConstraints, Class permissionClass, ClassLoader loader) {
        super(serverConstraints, permissionClass, loader);
        this.serverConstraints = serverConstraints;
        this.permissionClass = permissionClass;
    }

    public void setReportMap(Map<String, Integer> reportMap) {
        this.reportMap = reportMap;
    }

    @Override
    protected InvocationDispatcher createInvocationDispatcher(Collection methods, Remote impl, ServerCapabilities caps) throws ExportException {
        if(impl == null) {
            throw new NullPointerException("impl is null");
        }
        return new DebugInvocationDispatcher(methods, caps,
                serverConstraints,
                permissionClass,
                getClassLoader(), reportMap);
    }

    @Override
    protected InvocationHandler createInvocationHandler(Class[] interfaces, Remote impl, ObjectEndpoint oe) throws ExportException {
        for(Class iface : interfaces) {
            if(iface == null) {
                throw new NullPointerException();
            }
        }
        if(impl == null) {
            throw new NullPointerException();
        }
        return new DebugInvocationHandler(oe, serverConstraints, reportMap);
    }
}
