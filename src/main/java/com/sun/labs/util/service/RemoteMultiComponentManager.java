/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.util.service;

import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.logging.Logger;

/**
 * A component manager that will handle multiple instances of a given component
 * type and hand them out in a round-robin fashion when requested.
 */
public class RemoteMultiComponentManager<T extends Configurable> extends RemoteComponentManager<T> {

    private T[] components;

    int p;

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
    public T getComponent() {
        if(components == null) {
            getComponents();
        }
        synchronized(this) {
            if (components.length == 0) {
                throw new PropertyException(new ClassNotFoundException("Error finding class " + clazz.getName()));
            }
            p %= components.length;
            return components[p++];
        }
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

    public void shutdown() {
        cm.shutdown();
    }
}
