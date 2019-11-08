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

import com.oracle.labs.mlrg.olcut.config.io.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.property.Property;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Carrier for property data. Principally a {@link Map} from {@link String} to {@link Property}, and
 * a class name. Also includes configuration for loading an object over RMI via Jini.
 */
public final class ConfigurationData implements Serializable {
    private static final long serialVersionUID = 1L;

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

    /**
     * The time to lease this object.
     */
    private final long leaseTime;

    /**
     * The (possibly <code>null</code>) name of a component that has a list
     * of configuration entries to use when registering this component with
     * a service registrar.
     */
    private final String entriesName;

    /**
     * Creates an empty ConfigurationData.
     * @param name      the name of the item
     * @param className the class name of the item
     */
    public ConfigurationData(String name, String className) {
        this(name, className, Collections.emptyMap());
    }

    /**
     * Creates a ConfigurationData with the specified properties. The properties are validated elsewhere as
     * this does not trigger class loading.
     * @param name The name of the configured object.
     * @param className The class name of the configured object.
     * @param properties The properties to apply to that object.
     */
    public ConfigurationData(String name, String className, Map<String, Property> properties) {
        this(name,className,properties,null,null,false,false,DEFAULT_LEASE_TIME);
    }

    /**
     * Creates a ConfigurationData with no properties.
     * @param name The name of the configured object.
     * @param className The class name of the configured object.
     * @param serializedForm A path to load the serialised form of this object (or null).
     * @param entriesName The entries to restrict Jini loading (or null).
     * @param exportable Is this object exportable via a Jini registry.
     * @param importable Should this object be imported via a Jini registry.
     * @param leaseTime How long before the Jini registrar needs to have the object renewed.
     */
    public ConfigurationData(String name, String className, String serializedForm, String entriesName, boolean exportable, boolean importable, long leaseTime) {
        this(name,className, Collections.emptyMap(),serializedForm,entriesName,exportable,importable,leaseTime);
    }

    /**
     * Creates a ConfigurationData with the specified properties. The properties are validated elsewhere as
     * this does not trigger class loading.
     * @param name The name of the configured object.
     * @param className The class name of the configured object.
     * @param properties The properties to apply to that object.
     * @param serializedForm A path to load the serialised form of this object (or null).
     * @param entriesName The entries to restrict Jini loading (or null).
     * @param exportable Is this object exportable via a Jini registry.
     * @param importable Should this object be imported via a Jini registry.
     * @param leaseTime How long before the Jini registrar needs to have the object renewed.
     */
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

    /**
     * Returns the path to the serialised form.
     * @return The path to the serialised form.
     */
    public String getSerializedForm() {
        return serializedForm;
    }

    /**
     * Should this configuration import a remote object via Jini.
     * @return Should the configuration import a remote object.
     */
    public boolean isImportable() {
        return importable;
    }

    /**
     * Returns the Jini lease time. Defaults to -1, Leases.ANY.
     * @return The Jini lease time.
     */
    public long getLeaseTime() {
        return leaseTime;
    }

    /**
     * Should this configuration export it's object via Jini.
     * @return Should the configuration export a remote object.
     */
    public boolean isExportable() {
        return exportable;
    }

    /**
     * Returns the entries which control Jini lookup.
     * @return The Jini control entries.
     */
    public String getEntriesName() {
        return entriesName;
    }

    /** @return Returns an unmodifiable view on the properties. */
    public Map<String, Property> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Returns the value associated with that property name, or {@link Optional#empty}
     * if it doesn't exist.
     * @param propertyName The property name.
     * @return The {@link Optional#of} the property value or optional empty.
     */
    public Optional<Property> get(String propertyName) {
        Property value = properties.get(propertyName);
        if (value == null) {
            return Optional.empty();
        } else {
            return Optional.of(value);
        }
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

    /**
     * Copies this ConfigurationData. The copy is disconnected from the original as
     * it contains a different map (though all the elements are immutable and the same references).
     * @return A copy of this object.
     */
    public ConfigurationData copy() {
        return new ConfigurationData(name,className,properties,serializedForm,entriesName,exportable,importable,leaseTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationData)) return false;
        ConfigurationData that = (ConfigurationData) o;
        return exportable == that.exportable &&
                importable == that.importable &&
                leaseTime == that.leaseTime &&
                name.equals(that.name) &&
                className.equals(that.className) &&
                properties.equals(that.properties) &&
                Objects.equals(serializedForm, that.serializedForm) &&
                Objects.equals(entriesName, that.entriesName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, className, properties, serializedForm, exportable, importable, leaseTime, entriesName);
    }

    /**
     * Writes out the configuration data.
     * @param configWriter The writer to use.
     * @throws ConfigWriterException If the writer throws an exception.
     */
    public void save(ConfigWriter configWriter) throws ConfigWriterException {
        save(configWriter,Collections.emptySet());
    }

    /**
     * Writes out the configuration data, redacting (i.e.\ ignoring) fields if necessary.
     * @param configWriter The writer to use.
     * @param redactedFields The fields to redact.
     * @throws ConfigWriterException If the writer throws an exception.
     */
    public void save(ConfigWriter configWriter, Set<String> redactedFields) {
        Map<String,String> attributes = new HashMap<>();

        attributes.put(ConfigLoader.NAME,name);
        attributes.put(ConfigLoader.TYPE,className);
        attributes.put(ConfigLoader.IMPORT,""+isImportable());
        attributes.put(ConfigLoader.EXPORT,""+isExportable());
        if (getLeaseTime() > 0) {
            attributes.put(ConfigLoader.LEASETIME, "" + getLeaseTime());
        }
        if (getSerializedForm() != null) {
            attributes.put(ConfigLoader.SERIALIZED,getSerializedForm());
        }
        if (getEntriesName() != null) {
            attributes.put(ConfigLoader.ENTRIES,getEntriesName());
        }

        Map<String,Property> writtenProperties = new HashMap<>();
        for (Map.Entry<String,Property> p : properties.entrySet()) {
            if (!redactedFields.contains(p.getKey())) {
                writtenProperties.put(p.getKey(),p.getValue());
            }
        }

        configWriter.writeComponent(attributes,writtenProperties);
    }

}
