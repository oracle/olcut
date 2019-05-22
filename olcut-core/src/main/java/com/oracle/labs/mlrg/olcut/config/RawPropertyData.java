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

import java.util.HashMap;
import java.util.Map;

/** Holds the raw property data just as it has come in from the properties file. */
public class RawPropertyData {

    private final String name;

    private final String className;

    private final Map<String, Property> properties;
    
    /**
     * A URL for a resource indicating from where the component can be
     * deserialized.
     */
    private String serializedForm;

    /**
     * Whether this component is exportable to a service registrar.
     */
    private boolean exportable;
    
    private boolean importable;
    
    private long leaseTime;
    
    /**
     * The (possibly <code>null</code> name of a component that has a list
     * of configuration entries to use when registering this component with
     * a service registrar.
     */
    private String entriesName;

    /**
     * Creates a raw property data item
     *
     * @param name      the name of the item
     * @param className the class name of the item
     */
    public RawPropertyData(String name, String className) {
        this(name, className, null);
    }

    public RawPropertyData(String name, String className, Map<String, Property> properties) {
        this.name = name;
        this.className = className;
        if(properties != null) {
            this.properties = new HashMap<>(properties);
        } else {
            this.properties = new HashMap<>();
        }
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

    public void setSerializedForm(String serializedForm) {
        this.serializedForm = serializedForm;
    }

    public void setLeaseTime(long leaseTime) {
        this.leaseTime = leaseTime;
    }
    
    public void setImportable(boolean importable) {
        this.importable = importable;
    }
    
    public boolean isImportable() {
        return importable;
    }
    
    public long getLeaseTime() {
        return leaseTime;
    }
    
    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }
    
    public boolean isExportable() {
        return exportable;
    }
    
    public void setEntriesName(String entriesName) {
        this.entriesName = entriesName;
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
        return properties.get(propName) != null;
    }

    /** 
     * Returns a copy of this property data instance with all ${}-fields resolved.
     */
    public RawPropertyData flatten(ConfigurationManager cm) {
        RawPropertyData copyRPD = new RawPropertyData(name, className);

        for(Map.Entry<String, Property> e : properties.entrySet()) {
            String propName = e.getKey();
            Property propVal = e.getValue();
            if(propVal instanceof SimpleProperty) {
                propVal = new SimpleProperty(cm.getImmutableGlobalProperties().
                        replaceGlobalProperties(getName(), propName, ((SimpleProperty) propVal).getValue()));
            }

            copyRPD.properties.put(propName, propVal);
        }

        return copyRPD;
    }
}
