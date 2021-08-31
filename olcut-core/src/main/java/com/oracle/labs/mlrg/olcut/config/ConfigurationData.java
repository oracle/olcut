/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2003 Mitsubishi Electric Research Laboratories.
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

import com.oracle.labs.mlrg.olcut.config.io.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.Property;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Carrier for property data. Principally a {@link Map} from {@link String} to {@link Property}, and
 * a class name.
 */
public final class ConfigurationData implements Serializable {
    private static final Logger logger = Logger.getLogger(ConfigurationData.class.getName());

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
    @Deprecated
    public boolean isImportable() {
        return importable;
    }

    /**
     * Returns the Jini lease time. Defaults to -1, Leases.ANY.
     * @return The Jini lease time.
     */
    @Deprecated
    public long getLeaseTime() {
        return leaseTime;
    }

    /**
     * Should this configuration export it's object via Jini.
     * @return Should the configuration export a remote object.
     */
    @Deprecated
    public boolean isExportable() {
        return exportable;
    }

    /**
     * Returns the entries which control Jini lookup.
     * @return The Jini control entries.
     */
    @Deprecated
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

    @Override
    public String toString() {
        return "ConfigurationData(" +
                "name='" + name + '\'' +
                ", className='" + className + '\'' +
                ", properties=" + properties +
                ", serializedForm='" + serializedForm + '\'' +
                ", exportable=" + exportable +
                ", importable=" + importable +
                ", leaseTime=" + leaseTime +
                ", entriesName='" + entriesName + '\'' +
                ')';
    }

    /**
     * Checks whether a pair of SimplePropertys are equal to one another, dereferencing and recursively traversing
     * ConfigurationData as necessary.
     */
    private static boolean simplePropertyDerefEquals(Map<String, ConfigurationData> a, Map<String, ConfigurationData> b, SimpleProperty aSimple, SimpleProperty bSimple, String propName, Optional<String> locationContext) {
        Optional<ConfigurationData> aDerefOpt = Optional.ofNullable(a.get(aSimple.getValue()));
        Optional<ConfigurationData> bDerefOpt = Optional.ofNullable(b.get(bSimple.getValue()));
        if(aDerefOpt.isPresent() && bDerefOpt.isPresent()) {
            // both are references
            boolean eq = innerStructuralEquals(a, b, aSimple.getValue(), bSimple.getValue());
            if(!eq) {
                logger.fine(String.format("Property key: %s%s, a does not structurally equal b", propName, locationContext.orElse("")));
            }
            return eq;
        } else if ((!aDerefOpt.isPresent()) && (!bDerefOpt.isPresent())) {
            // both are not references
            boolean valueMatch = aSimple.equals(bSimple);
            if(!valueMatch) {
                logger.fine(String.format("Property key: %s%s, a.value: %s, b.value: %s",
                        propName, locationContext.orElse(""), aSimple.getValue(), bSimple.getValue()));
            }
            return valueMatch;
        } else {
            // mismatch between reference and non-reference
            if(aDerefOpt.isPresent()) {
                logger.fine(String.format("Property key: %s%s a (%s) is a reference while b (%s) is a value", propName, locationContext.orElse(""), aSimple.getValue(), bSimple.getValue()));
            } else {
                logger.fine(String.format("Property key: %s%s a (%s) is a value while b (%s) is a reference", propName, locationContext.orElse(""), aSimple.getValue(), bSimple.getValue()));
            }
            return false;
        }
    }

    /**
     * Checks whether a property name between two ConfigurationData objects 'matches', whether it represents an equal
     * value or, recursively whether it is structurally equal.
     */
    private static boolean propertyNamesMatch (String propName, Map<String, ConfigurationData> a, Map<String, ConfigurationData> b, ConfigurationData aRoot, ConfigurationData bRoot) {
        Optional<Property> aPropOpt = Optional.ofNullable(aRoot.getProperties().get(propName));
        Optional<Property> bPropOpt = Optional.ofNullable(bRoot.getProperties().get(propName));
        if(aPropOpt.isPresent() && bPropOpt.isPresent()) {
            Property aProp = aPropOpt.get();
            Property bProp = bPropOpt.get();
            if(aProp instanceof SimpleProperty && bProp instanceof SimpleProperty) {
                return simplePropertyDerefEquals(a, b, (SimpleProperty) aProp, (SimpleProperty) bProp, propName, Optional.empty());
            } else if(aProp instanceof ListProperty && bProp instanceof ListProperty) {
                List<SimpleProperty> aList = ((ListProperty) aProp).getSimpleList();
                List<SimpleProperty> bList = ((ListProperty) bProp).getSimpleList();
                return aList.size() == bList.size() &&
                        IntStream.range(0, aList.size()).allMatch(i -> simplePropertyDerefEquals(a, b, aList.get(i), bList.get(i), propName, Optional.of(", List index: " + i)));
            } else if(aProp instanceof MapProperty && bProp instanceof MapProperty) {
                Map<String, SimpleProperty> aMap = ((MapProperty) aProp).getMap();
                Map<String, SimpleProperty> bMap = ((MapProperty) bProp).getMap();
                return aMap.keySet().equals(bMap.keySet()) &&
                        aMap.keySet().stream().allMatch(k -> simplePropertyDerefEquals(a, b, aMap.get(k), bMap.get(k), propName, Optional.of(", PropValue key: " + k)));
            } else {
                logger.severe(String.format("Unrecognized Property type: %s with name: %s", aProp.getClass().getName(), propName));
                return false;
            }
        } else if((!aPropOpt.isPresent()) && (!bPropOpt.isPresent())) {
            return true; // both missing is a match
        } else {
            logger.fine(String.format("Property key: %s, aOpt: %s, bOpt: %s", propName, aPropOpt, bPropOpt));
            return false;
        }
    }

    /**
     * See {@link #structuralEquals(List, List, String, String)} for description of behavior.
     */
    private static boolean innerStructuralEquals(Map<String, ConfigurationData> a, Map<String, ConfigurationData> b, String aName, String bName) {
        Optional<ConfigurationData> aRootOpt = Optional.ofNullable(a.get(aName));
        Optional<ConfigurationData> bRootOpt = Optional.ofNullable(b.get(bName));
        if((!aRootOpt.isPresent()) || (!bRootOpt.isPresent())) {
            if(!aRootOpt.isPresent()) {
                logger.fine(String.format("%s is not found", aName));
            } else {
                logger.fine(String.format("%s is not found", bName));
            }
            return false;
        } else if(aRootOpt.get().equals(bRootOpt.get())) {
            logger.fine(String.format("%s and %s are .equals()", aName, bName));
            return true;
        } else {
            ConfigurationData aRoot = aRootOpt.get();
            ConfigurationData bRoot = bRootOpt.get();

            Set<String> propNames = new HashSet<>(aRoot.getProperties().keySet());
            propNames.addAll(bRoot.getProperties().keySet());

            boolean typesMatch = aRoot.getClassName().equals(bRoot.getClassName());
            if(!typesMatch) {
                logger.fine(String.format("type mismatch, a.class: %s b.class: %s", aRoot.getClassName(), bRoot.getClassName()));
            }

            return typesMatch &&
                    propNames.stream().allMatch(propName -> propertyNamesMatch(propName, a, b, aRoot, bRoot));
        }
    }

    /**
     * Checks whether two ConfigurationData objects are 'structurally equal'. Two objects are structurally
     * equal when their classNames are the same and when all of their properties are equal or, if those
     * properties refer to another ConfigurationData by name, if all of their properties are structurally
     * equal recursively.
     *
     * <p>
     *
     * {@code aName} should be the name of an element of {@code a} that is to be compared to {@code bName}
     * in {@code b}. {@code a} and {@code b} should each contain all the ConfigurationData objects
     * needed to instantiate the objects named by {@code aName} and {@code bName} respectively. Objects not
     * instantiated by traversing children of {@code aName} and {@code bName} are ignored.
     *
     * <p>
     *
     * At log-level {@code FINE} this reports the first place where the two instances differ, and the nature of their
     * difference.
     *
     * @param a ConfigurationData List for the first object and its children
     * @param b ConfigurationData List for the second object and its children
     * @param aName Name of the first object
     * @param bName Name of the second object
     * @return {@code true} if class and all values of {@code aName} and {@code bName} are the same once
     * they have been dereferenced by name according to {@code a} and {@code b}.
     */
    public static boolean structuralEquals(List<ConfigurationData> a, List<ConfigurationData> b , String aName, String bName) {
        return innerStructuralEquals(
                a.stream().collect(Collectors.toMap(ConfigurationData::getName, Function.identity())),
                b.stream().collect(Collectors.toMap(ConfigurationData::getName, Function.identity())),
                aName, bName);
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
