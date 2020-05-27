
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

import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.logging.Logger;

/**
 * A component manager that will handle multiple instances of a given component
 * type and hand them out in a round-robin fashion when requested.
 */
public class RemoteMultiComponentManager<T extends Configurable> extends RemoteComponentManager<T> {

    private T[] components;

    private int p;

    private static final Logger logger = Logger.getLogger(RemoteMultiComponentManager.class.getName());
    
    public RemoteMultiComponentManager(ConfigurationManager cm, Class<T> c) {
        super(cm, c);
    }

    private synchronized void getComponents() {
        List<T> l = cm.lookupAll(clazz, this);
        if(l != null) {
            components = l.toArray((T[])Array.newInstance(clazz, 0));
        }
    }
    
    /**
     * Gets the component with the given name
     * @return the component
     */
    public synchronized T getComponent() {
        if(components == null) {
            getComponents();
        }
        if (components.length == 0) {
            throw new PropertyException(new ClassNotFoundException("Error finding class " + clazz.getName()),"","Error finding instances of class " + clazz.getName());
        }
        p %= components.length;
        return components[p++];
    }

    @Override
    public void componentAdded(Configurable added) {
        logger.info("Added: " + added);
        getComponents();
    }

    @Override
    public void componentRemoved(Configurable componentToRemove) {
        logger.info("Removed: " + componentToRemove);
        getComponents();
    }

    @Override
    public void close() {
        cm.close();
    }
}
