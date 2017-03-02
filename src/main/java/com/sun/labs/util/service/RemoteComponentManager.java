/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ComponentListener;
import com.sun.labs.util.props.ConfigurationManager;

/**
 * Manages connecting and reconnecting to a component
 */
public class RemoteComponentManager<T extends Component> implements ComponentListener {
    protected ConfigurationManager cm;
    protected Class clazz;
    private T component = null;

    /**
     * Creates a RemoteComponentManager
     * @param cm the configuration manager to use to fetch components
     * @param logger the logger to use (or null, in which case an anonymous logger will be used)
     */
    public RemoteComponentManager(ConfigurationManager cm, Class c) {
        this.cm = cm;
        this.clazz = c;
    }

    private T lookup() {
        return (T) cm.lookup(clazz, this);
    }

    /**
     * Gets the component with the given name
     * @return the component
     * @throws com.sun.labs.aura.util.AuraException if the component could not
     *   be found after 10 minutes
     */
    public T getComponent() {
        if(component == null) {
            component = (T) cm.lookup(clazz, this);
        }
        return component;
    }


    @Override
    public void componentAdded(Component c) {
        if(component == null && clazz.isAssignableFrom(c.getClass())) {
            component = (T) c;
        }
    }

    @Override
    public void componentRemoved(Component componentToRemove) {
        if (component == componentToRemove) {
            component = null;
        }
    }

    public void shutdown() {
        cm.shutdown();
    }
}
