/*
 * 
 * Copyright 1999-2004 Carnegie Mellon University.  
 * Portions Copyright 2004 Sun Microsystems, Inc.  
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */
package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.config.property.Property;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Carrier for property data. Principally a {@link Map} from {@link String} to {@link Property}, and
 * a class name. Also includes configuration for loading an object over RMI via Jini.
 */
public class ConfigurationData implements Serializable {

    public static final long DEFAULT_LEASE_TIME = -1;

    private final String name;

    private final String className;

    private final Map<String, Property> properties;
    
    /**
     * A URL for a resource indicating from where the component can be
     * deserialized.
     */
    private final String serializedForm;

    /**
     * Whether this component is exportable to a service registrar.
     */
    private final boolean exportable;
    
    private final boolean importable;
    
    private final long leaseTime;
    
    /**
     * The (possibly <code>null</code>) name of a component that has a list
     * of configuration entries to use when registering this component with
     * a service registrar.
     */
    private final String entriesName;

    /**
     * Creates a raw property data item
     *
     * @param name      the name of the item
     * @param className the class name of the item
     */
    public ConfigurationData(String name, String className) {
        this(name, className, Collections.emptyMap());
    }

    public ConfigurationData(String name, String className, Map<String, Property> properties) {
        this(name,className,properties,null,null,false,false,DEFAULT_LEASE_TIME);
    }

    public ConfigurationData(String name, String className, String serializedForm, String entriesName, boolean exportable, boolean importable, long leaseTime) {
        this(name,className, Collections.emptyMap(),serializedForm,entriesName,exportable,importable,leaseTime);
    }

    public ConfigurationData(String name, String className, Map<String, Property> properties, String serializedForm, String entriesName, boolean exportable, boolean importable, long leaseTime) {
        this.name = name;
        this.className = className;
        this.properties = new HashMap<>(properties);
        this.serializedForm = serializedForm;
        this.entriesName = entriesName;
        this.exportable = exportable;
        this.importable = importable;
        this.leaseTime = leaseTime;
    }

    /**
     * Adds a new property
     *
     * @param propName  the name of the property
     * @param propValue the value of the property
     */
    public void add(String propName, Property propValue) {
        properties.put(propName, propValue);
    }

    /** @return Returns the className. */
    public String getClassName() {
        return className;
    }

    /** @return Returns the name. */
    public String getName() {
        return name;
    }

    public String getSerializedForm() {
        return serializedForm;
    }

    public boolean isImportable() {
        return importable;
    }
    
    public long getLeaseTime() {
        return leaseTime;
    }
    
    public boolean isExportable() {
        return exportable;
    }
    
    public String getEntriesName() {
        return entriesName;
    }

    /** @return Returns the properties. */
    public Map<String, Property> getProperties() {
        return properties;
    }

    /**
     * Determines if the map already contains an entry for this property
     *
     * @param propName the property of interest
     * @return true if the map already contains this property
     */
    public boolean contains(String propName) {
        return properties.containsKey(propName);
    }

}
