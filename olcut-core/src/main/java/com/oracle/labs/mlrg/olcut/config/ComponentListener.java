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

package com.oracle.labs.mlrg.olcut.config;

/**
 * An interface that can be used for changes to components that have been 
 * looked up in a configuration manager, especially for those components that 
 * have been looked up in a component registry.
 * 
 * @see ConfigurationManager#lookup(String,ComponentListener)
 * @see ConfigurationManager#lookupAll(Class, ComponentListener)
 */
public interface ComponentListener<T extends Configurable> {
    
    
    /**
     * Indicates that a component has been added to the configuration.  This
     * method will only be called when a component of a type that the listener
     * looked up has been added.  Note that this method will be called with any
     * component of the appropriate type.  It is up to the implementer to decide
     * whether it is interested in any particular instance of a component.
     * @param c the component that was added
     */
    public void componentAdded(T c);
    
    /**
     * Indicates that a component has been removed from the configuration.  This 
     * method will be called when any component of a type that the listener looked
     * up has been removed.
     * @param c the component that was removed.  Note that it's not likely that you
     * can do much with this component except test it's class.
     */
    public void componentRemoved(T c);
    
}
