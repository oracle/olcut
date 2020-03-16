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
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;

import java.rmi.Remote;
import java.util.logging.Logger;

/**
 * A property sheet which defines a collection of properties for a single
 * component in the system.
 */
public class ServablePropertySheet<T extends Configurable> extends PropertySheet<T> {
    private static final Logger logger = Logger.getLogger(ServablePropertySheet.class.getName());

    private final boolean implementsRemote;

    /**
     * The configuration entries to use for service registration or matching.
     */
    private final ConfigurationEntry[] entries;

    protected ServablePropertySheet(T configurable,
                                 JiniConfigurationManager cm, ConfigurationData rpd) {
        this((Class<T>) configurable.getClass(), cm, rpd);
        owner = configurable;
    }

    protected ServablePropertySheet(Class<T> confClass,
                                 JiniConfigurationManager cm, ConfigurationData rpd) {
        super(confClass,cm,rpd);

        //
        // Does this class implement remote?
        implementsRemote = Remote.class.isAssignableFrom(ownerClass);

        //
        // If we're supposed to have configuration entries, then get them now.
        if (data.getEntriesName() != null) {
            ConfigurationEntries ce
                    = (ConfigurationEntries) cm.lookup(data.getEntriesName());
            if (ce == null) {
                throw new PropertyException(instanceName, "entries",
                        "Cannot find entries component " + data.getEntriesName());
            }
            entries = ce.getEntries();
        } else {
            entries = null;
        }
    }

    protected ServablePropertySheet(ServablePropertySheet<T> other){
        super(other);

        //
        // Does this class implement remote?
        implementsRemote = Remote.class.isAssignableFrom(other.ownerClass);

        //
        // If we're supposed to have configuration entries, then get them now.
        if (data.getEntriesName() != null) {
            ConfigurationEntries ce
                    = (ConfigurationEntries) cm.lookup(data.getEntriesName());
            if (ce == null) {
                throw new PropertyException(instanceName, "entries",
                        "Cannot find entries component " + data.getEntriesName());
            }
            entries = ce.getEntries();
        } else {
            entries = null;
        }
    }

    public boolean implementsRemote() {
        return implementsRemote;
    }

    public ConfigurationEntry[] getEntries() {
        return entries;
    }

    @Override
    public synchronized T getOwner(ComponentListener<T> cl, boolean reuseComponent) {
        if (!isInstantiated() || !reuseComponent) {

            ComponentRegistry registry = ((JiniConfigurationManager)cm).getComponentRegistry();
            //
            // See if we should do a lookup in a service registry.
            if (registry != null
                    && !isExportable()
                    && ((size() == 0 && implementsRemote) || isImportable())) {
                    logger.finer(String.format("Looking up instance %s in registry",
                            getInstanceName()));
                owner = registry.lookup(this, cl);
                if (owner != null) {
                    return owner;
                } else if (size() == 0 && isImportable()) {
                    //
                    // We needed to look something up and no success,
                    // so throw exception.
                    throw new PropertyException(instanceName,"Failed to lookup instance.");
                }
            }

            // Failed to do service lookup, instantiate directly
            super.getOwner(cl, reuseComponent);

            if (registry != null && isExportable()) {
                registry.register(owner, this);
            }
        }

        return owner;
    }

    /**
     * Gets the owning property manager
     *
     * @return the property manager
     */
    @Override
    public JiniConfigurationManager getConfigurationManager() {
        return (JiniConfigurationManager) cm;
    }

    @Override
    public void setCM(ConfigurationManager cm) {
        if (cm instanceof JiniConfigurationManager) {
            this.cm = cm;
        } else {
            throw new IllegalArgumentException("Must pass a JiniConfigurationManager to a ServablePropertySheet");
        }
    }

    @Override
    public ServablePropertySheet<T> copy() {
        return new ServablePropertySheet<>(this);
    }

}
