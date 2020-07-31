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

import com.oracle.labs.mlrg.olcut.config.ComponentListener;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;

import java.io.Closeable;

/**
 * Manages connecting and reconnecting to a component
 */
public class RemoteComponentManager<T extends Configurable> implements Closeable, ComponentListener<T> {
    protected ConfigurationManager cm;
    protected Class<T> clazz;
    private T component = null;

    /**
     * Creates a RemoteComponentManager
     * @param cm the configuration manager to use to fetch components
     * @param c The class this component manager looks after.
     */
    public RemoteComponentManager(ConfigurationManager cm, Class<T> c) {
        this.cm = cm;
        this.clazz = c;
    }

    private T lookup() {
        return cm.lookup(clazz, this);
    }

    /**
     * Gets the component with the given name
     * @return the component
     */
    public T getComponent() {
        if(component == null) {
            component = cm.lookup(clazz, this);
        }
        return component;
    }

    @Override
    public void componentAdded(T c) {
        if(component == null && clazz.isAssignableFrom(c.getClass())) {
            component = c;
        }
    }

    @Override
    public void componentRemoved(T componentToRemove) {
        if (component == componentToRemove) {
            component = null;
        }
    }

    @Override
    public void close() {
        cm.close();
    }
}
