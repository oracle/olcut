/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2003 Mitsubishi Electric Research Laboratories.
 * Copyright (c) 2004, 2025, Oracle and/or its affiliates.
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
import com.oracle.labs.mlrg.olcut.util.Util;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Carrier for property data. Principally a {@link Map} from {@link String} to {@link Property}, and
 * a class name.
 *
 * @param name           The name of the configured object.
 * @param className      The class name of the configured object.
 * @param properties     The properties to apply to that object.
 * @param serializedForm A URL for a resource indicating from where the component can be
 *                       deserialized.
 */
public record ConfigurationData(String name, String className, Map<String, Property> properties,
                                String serializedForm) {
    private static final Logger logger = Logger.getLogger(ConfigurationData.class.getName());

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
        this(name,className,properties,null);
    }

    /**
     * Creates a ConfigurationData with no properties.
     * @param name The name of the configured object.
     * @param className The class name of the configured object.
     * @param serializedForm A path to load the serialised form of this object (or null).
     */
    public ConfigurationData(String name, String className, String serializedForm) {
        this(name,className,Collections.emptyMap(),serializedForm);
    }

    /**
     * Creates a ConfigurationData with the specified properties. The properties are validated elsewhere as
     * this does not trigger class loading.
     *
     * @param name           The name of the configured object.
     * @param className      The class name of the configured object.
     * @param properties     The properties to apply to that object.
     * @param serializedForm A path to load the serialised form of this object (or null).
     */
    public ConfigurationData(String name, String className, Map<String, Property> properties, String serializedForm) {
        this.name = name;
        this.className = className;
        this.properties = new HashMap<>(properties);
        this.serializedForm = serializedForm;
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

    /**
     * @return Returns an unmodifiable view on the properties.
     */
    @Override
    public Map<String, Property> properties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Returns the value associated with that property name, or {@link Optional#empty}
     * if it doesn't exist.
     *
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
     *
     * @return A copy of this object.
     */
    public ConfigurationData copy() {
        return new ConfigurationData(name, className, properties, serializedForm);
    }

    @Override
    public String toString() {
        return "ConfigurationData(" +
                "name='" + name + '\'' +
                ", className='" + className + '\'' +
                ", properties=" + properties +
                ", serializedForm='" + serializedForm + '\'' +
                ')';
    }

    /**
     * Supporting class for {@link ConfigurationData#structuralEquals(List, List, String, String)}. This class defines
     * equality semantics for an instance of {@link ConfigurationData} by checking className equality and checking for
     * equality between dereferenced property lists.
     */
    private static class StructuralConfigurationData {
        private final String name;
        private final String className;

        private final Map<String, DerefedProperty> simpleProperties;
        private final Map<String, List<DerefedProperty>> listProperties;
        private final Map<String, List<Class<?>>> listClassProperties;
        private final Map<String, Map<String, DerefedProperty>> mapProperties;

        public static Optional<StructuralConfigurationData> fromName(Map<String, ConfigurationData> contextMap, String name) {
            return Optional.ofNullable(contextMap.get(name))
                    .map(configurationData ->
                            new StructuralConfigurationData(contextMap, configurationData));
        }

        public StructuralConfigurationData(Map<String, ConfigurationData> contextMap, ConfigurationData referencedCD) {
            this.name = referencedCD.name;
            this.className = referencedCD.className;
            this.simpleProperties = new HashMap<>();
            this.listProperties = new HashMap<>();
            this.listClassProperties = new HashMap<>();
            this.mapProperties = new HashMap<>();
            for (Map.Entry<String, Property> propertyEntry : referencedCD.properties.entrySet()) {
                String propName = propertyEntry.getKey();
                Property prop = propertyEntry.getValue();
                switch (prop) {
                    case SimpleProperty simpleProperty ->
                            this.simpleProperties.put(propName, new DerefedProperty(contextMap, simpleProperty, propName, ""));
                    case ListProperty listProperty -> {
                        this.listProperties.put(propName,
                                IntStream.range(0, listProperty.simpleList().size())
                                        .mapToObj(i ->
                                                new DerefedProperty(contextMap, listProperty.simpleList().get(i), propName, ", index: " + i))
                                        .collect(Collectors.toList()));
                        this.listClassProperties.put(propName, listProperty.classList());
                    }
                    case MapProperty mapProperty ->
                            this.mapProperties.put(propName, mapProperty.map().entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey,
                                            e -> new DerefedProperty(contextMap, e.getValue(),
                                                    propName, ", PropValue key: " + e.getKey()))));
                    case null, default ->
                            logger.fine(String.format("Unknown Property of key: %s with type: %s", propertyEntry.getKey(), prop.getClass()));
                }
            }
        }

        private static <T> boolean checkPresenceAllMatch(Map<String, T> aMap, Map<String, T> bMap, BiPredicate<T, T> matchPair) {
            Set<String> allKeys = new HashSet<>(aMap.keySet());
            allKeys.addAll(bMap.keySet());
            return allKeys.stream().allMatch(k -> {
                Optional<T> maybeA = Optional.ofNullable(aMap.get(k));
                Optional<T> maybeB = Optional.ofNullable(bMap.get(k));
                if (maybeA.isPresent() && maybeB.isPresent()) {
                    return matchPair.test(maybeA.get(), maybeB.get());
                } else {
                    if (maybeA.isPresent()) {
                        logger.fine(String.format("Missing property: a: %s, no value for b", k));
                    } else {
                        logger.fine(String.format("Missing property: b: %s, no value for a", k));
                    }
                    return false;
                }
            });
        }

        /**
         * Note that this does not check the {@code name} field deliberately.
         */
        @Override
        public int hashCode() {
            return Objects.hash(className, simpleProperties, listProperties, listClassProperties, mapProperties);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof StructuralConfigurationData that) {
                if (this.className.equals(that.className)) {
                    boolean simpleMatch = checkPresenceAllMatch(this.simpleProperties, that.simpleProperties,
                            DerefedProperty::equals);

                    if (!simpleMatch) {
                        logger.fine("SimpleProperties don't match: as: " + this.simpleProperties + " bs: " + that.simpleProperties);
                    }

                    boolean mapMatch = checkPresenceAllMatch(this.mapProperties, that.mapProperties,
                            (aMap, bMap) ->
                                    aMap.keySet().equals(bMap.keySet()) &&
                                            aMap.keySet().stream()
                                                    .allMatch(k -> aMap.get(k).equals(bMap.get(k))));

                    if (!mapMatch) {
                        logger.fine("MapProperties don't match: as: " + this.mapProperties + " bs: " + that.mapProperties);
                    }

                    boolean listMatch = checkPresenceAllMatch(this.listProperties, that.listProperties, (as, bs) -> {
                        boolean eq = Util.bagEquality(as, bs);
                        if (!eq) {
                            logger.fine("ListProperties not equal using bag equality:\na: " + as.toString() + "\nb: " + bs.toString());
                        }
                        return eq;
                    }) &&
                            checkPresenceAllMatch(this.listClassProperties, that.listClassProperties, (as, bs) -> {
                                boolean eq = Util.bagEquality(as, bs);
                                if (!eq) {
                                    logger.fine("ListClassProperties not equal using bag equality:\na: " + as.toString() + "\nb: " + bs.toString());
                                }
                                return eq;
                            });

                    if (!listMatch) {
                        logger.fine("ListProperties don't match: as: " + this.listProperties + " bs: " + that.listProperties);
                        logger.fine("ListClassProperties don't match: as: " + this.listClassProperties + " bs: " + that.listClassProperties);
                    }

                    return simpleMatch && mapMatch && listMatch;
                } else {
                    logger.fine(String.format("type mismatch, a.class: %s b.class: %s", this.className, this.className));
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "StructuralConfigurationData(" +
                    "name='" + name + '\'' +
                    ", className='" + className + '\'' +
                    ", simpleProperties=" + simpleProperties +
                    ", listProperties=" + listProperties +
                    ", listClassProperties=" + listClassProperties +
                    ", mapProperties=" + mapProperties +
                    ')';
        }
    }

    /**
     * Supporting class for {@link ConfigurationData#structuralEquals(List, List, String, String)}. This class provides
     * equality semantics for individual instances of {@link SimpleProperty} that both dereference references and attempt
     * to deal with string representations of typed values in the way users typically expect. This leads to some strange
     * implementations, however. Details can be found on {@link DerefedProperty#equals(Object)}.
     */
    private static class DerefedProperty {

        private final boolean isDerefed;
        private StructuralConfigurationData innerConf;
        private final String innerValue;
        private final String propName;
        private final String locationContext;
        private Optional<Double> innerDoubleValue;
        private Optional<OffsetDateTime> innerDateTime;
        private Optional<OffsetTime> innerTime;
        private Optional<Boolean> innerBool;

        public DerefedProperty(Map<String, ConfigurationData> confs, SimpleProperty prop, String propName, String locationContext) {
            this.propName = propName;
            this.locationContext = locationContext;
            this.innerValue = prop.value();
            // we do it this way because Boolean.parseBoolean doesn't throw like other parse methods
            if (this.innerValue.trim().equalsIgnoreCase("true") || this.innerValue.trim().equalsIgnoreCase("false")) {
                this.innerBool = Optional.of(Boolean.parseBoolean(this.innerValue));
            } else {
                this.innerBool = Optional.empty();
            }
            try {
                this.innerDoubleValue = Optional.of(Double.parseDouble(this.innerValue));
            } catch (NumberFormatException e) {
                this.innerDoubleValue = Optional.empty();
            }
            try {
                this.innerDateTime = Optional.of(OffsetDateTime.parse(this.innerValue));
            } catch (DateTimeParseException e) {
                this.innerDateTime = Optional.empty();
            }
            try {
                this.innerTime = Optional.of(OffsetTime.parse(this.innerValue));
            } catch (DateTimeParseException e) {
                this.innerTime = Optional.empty();
            }
            Optional<ConfigurationData> maybeDeref = Optional.ofNullable(confs.get(prop.value()));
            isDerefed = maybeDeref.isPresent();
            if (isDerefed) {
                innerConf = new StructuralConfigurationData(confs, maybeDeref.get());
            }
        }

        public DerefedProperty(Map<String, ConfigurationData> confs, SimpleProperty prop, String propName) {
            this(confs, prop, propName, "");
        }

        private String reportString() {
            return String.format("Property Key: %s%s, with value %s %s %s %s %s", propName, locationContext,
                    innerValue, (isDerefed ? "(is a reference)" : "(is not a reference)"),
                    innerDoubleValue.map(d -> "(parsed as double " + d + ")").orElse("(not parsed as double)"),
                    innerDateTime.map(d -> "(parsed as date " + d + ")").orElse("(not parsed as date)"),
                    innerBool.map(d -> "(parsed as bool " + d + ")").orElse("(not parsed as bool)"));
        }

        @Override
        public int hashCode() {
            if (this.isDerefed) {
                return Objects.hash(this.isDerefed, this.innerConf);
            } else if (this.innerBool.isPresent()) {
                return Objects.hash(this.isDerefed, this.innerBool.get());
            } else if (this.innerDoubleValue.isPresent()) {
                return Objects.hash(this.isDerefed, this.innerDoubleValue.get());
            } else if (this.innerDateTime.isPresent()) {
                return Objects.hash(this.isDerefed, this.innerDateTime.get());
            } else if (this.innerTime.isPresent()) {
                return Objects.hash(this.isDerefed, this.innerTime.get());
            } else {
                return Objects.hash(this.isDerefed, this.innerValue);
            }
        }

        /**
         * To match the typical behavior a user would expect, this method uses a bad hack to try to infer types whose
         * string representation is likely to have different equality semantics from its typed representation. First
         * it attempts to resolve references and test them for equality. If that fails it will attempt to parse the
         * string as a boolean and test equality, then as a double and test for equality using
         * {@link Util#doubleEquals(double, double)}, then it will attempt to use
         * {@link OffsetDateTime#parse(CharSequence)} and test equality, followed by
         * {@link OffsetTime#parse(CharSequence)}. Finally, it will test using {@link String#equals(Object)}.
         *
         * @param o another object to equality test
         * @return true if the objects' values are equal according to the logic described above, false otherwise
         */
        @Override
        public boolean equals(Object o) {
            if (o instanceof DerefedProperty that) {
                if (this.isDerefed && that.isDerefed) {
                    return this.innerConf.equals(that.innerConf);
                } else if (!this.isDerefed && !that.isDerefed) {
                    boolean valueMatch;

                    if (this.innerBool.isPresent() && that.innerBool.isPresent()) {
                        valueMatch = this.innerBool.get().booleanValue() == that.innerBool.get().booleanValue();
                    } else if (this.innerDoubleValue.isPresent() && that.innerDoubleValue.isPresent()) {
                        valueMatch = Util.doubleEquals(this.innerDoubleValue.get(), that.innerDoubleValue.get());
                    } else if (this.innerDateTime.isPresent() && that.innerDateTime.isPresent()) {
                        valueMatch = this.innerDateTime.get().equals(that.innerDateTime.get());
                    } else if (this.innerTime.isPresent() && that.innerTime.isPresent()) {
                        valueMatch = this.innerTime.get().equals(that.innerTime.get());
                    } else {
                        valueMatch = this.innerValue.equals(that.innerValue);
                    }
                    if (!valueMatch) {
                        logger.fine(String.format("Property Value mismatch: %s and %s", this.reportString(), that.reportString()));
                    }
                    return valueMatch;
                } else {
                    logger.fine(String.format("Reference/Value mismatch: %s and %s", this.reportString(), that.reportString()));
                    return false;
                }
            } else {
                logger.fine("b is not a DerefedProperty b.class: " + o.getClass());
                return false;
            }
        }

        @Override
        public String toString() {
            return "DerefedProperty(" +
                    "isDerefed=" + isDerefed +
                    ", innerConf=" + innerConf +
                    ", innerValue='" + innerValue + '\'' +
                    ", propName='" + propName + '\'' +
                    ", locationContext='" + locationContext + '\'' +
                    ')';
        }
    }

    /**
     * See {@link #structuralEquals(List, List, String, String)} for description of behavior.
     */
    private static boolean innerStructuralEquals(Map<String, ConfigurationData> a, Map<String, ConfigurationData> b, String aName, String bName) {
        Optional<StructuralConfigurationData> aRootOpt = StructuralConfigurationData.fromName(a, aName);
        Optional<StructuralConfigurationData> bRootOpt = StructuralConfigurationData.fromName(b, bName);
        if (aRootOpt.isPresent() && bRootOpt.isPresent()) {
            return aRootOpt.get().equals(bRootOpt.get());
        } else {
            if (aRootOpt.isPresent()) {
                logger.fine(String.format("Component with name: %s not found in b", bName));
            } else {
                logger.fine(String.format("Component with name: %s not found in a", aName));
            }
            return false;
        }
    }

    /**
     * Checks whether two ConfigurationData objects are 'structurally equal'. Two objects are structurally
     * equal when their classNames are the same and when all of their properties are equal or, if those
     * properties refer to another ConfigurationData by name, if all of their properties are structurally
     * equal recursively. It does not compare {@link ConfigurationData#serializedForm} as the serialized form is not
     * relevant to the equality comparison we are making here.
     * <p>
     * N.B. Because the serialized configuration format internally represents things as strings with no type information
     * but equality semantics often differ between strings and the types the represent, the method attempts to intuit
     * represented types by attempting to parse the string as that type and treating it as that type if it successfully
     * parses. This means that, eg. ID values that are typed as strings but are fully numerical will be converted to
     * doubles and be compared for equality that way. For more details on the processing see {@link DerefedProperty#equals(Object)}.
     * <p>
     * {@code aName} should be the name of an element of {@code a} that is to be compared to {@code bName}
     * in {@code b}. {@code a} and {@code b} should each contain all the ConfigurationData objects
     * needed to instantiate the objects named by {@code aName} and {@code bName} respectively. Objects not
     * instantiated by traversing children of {@code aName} and {@code bName} are ignored.
     * <p>
     * At log-level {@link java.util.logging.Level#FINE} this reports the first configuration object where the two
     * instances differ, together with the property values that differ between those instances. Any time this method
     * returns {@code false}, it should also log at least one message.
     *
     * @param a     ConfigurationData List for the first object and its children
     * @param b     ConfigurationData List for the second object and its children
     * @param aName Name of the first object
     * @param bName Name of the second object
     * @return {@code true} if class and all values of {@code aName} and {@code bName} are the same once
     * they have been dereferenced by name according to {@code a} and {@code b}.
     */
    public static boolean structuralEquals(List<ConfigurationData> a, List<ConfigurationData> b, String aName, String bName) {
        return innerStructuralEquals(
                a.stream().collect(Collectors.toMap(ConfigurationData::name, Function.identity())),
                b.stream().collect(Collectors.toMap(ConfigurationData::name, Function.identity())),
                aName, bName);
    }

    /**
     * Writes out the configuration data.
     *
     * @param configWriter The writer to use.
     * @throws ConfigWriterException If the writer throws an exception.
     */
    public void save(ConfigWriter configWriter) throws ConfigWriterException {
        save(configWriter, Collections.emptySet());
    }

    /**
     * Writes out the configuration data, redacting (i.e.,\ ignoring) fields if necessary.
     *
     * @param configWriter   The writer to use.
     * @param redactedFields The fields to redact.
     * @throws ConfigWriterException If the writer throws an exception.
     */
    public void save(ConfigWriter configWriter, Set<String> redactedFields) {
        Map<String, String> attributes = new HashMap<>();

        attributes.put(ConfigLoader.NAME, name);
        attributes.put(ConfigLoader.TYPE, className);
        if (serializedForm() != null) {
            attributes.put(ConfigLoader.SERIALIZED, serializedForm());
        }

        Map<String, Property> writtenProperties = new HashMap<>();
        for (Map.Entry<String, Property> p : properties.entrySet()) {
            if (!redactedFields.contains(p.getKey())) {
                writtenProperties.put(p.getKey(), p.getValue());
            }
        }

        configWriter.writeComponent(attributes, writtenProperties);
    }

}
