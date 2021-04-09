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

package com.oracle.labs.mlrg.olcut.config.property;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * A collection of global properties used within a system configuration
 *
 * @see ConfigurationManager
 */
public class GlobalProperties extends ImmutableGlobalProperties {

    /**
     * Creates a GlobalProperties which only contains the built in properties.
     */
    public GlobalProperties() {
        super();
    }

    /**
     * Copies the supplied GlobalProperties.
     * @param globalProperties The properties to copy.
     */
    public GlobalProperties(GlobalProperties globalProperties) {
        super(globalProperties);
    }

    /**
     * Imports the system properties into this GlobalProperties.
     */
    public final void importSystemProperties() {
        Properties props = AccessController.doPrivileged((PrivilegedAction<Properties>) System::getProperties);
        importProperties(props);
    }

    /**
     * Imports the supplied properties.
     *
     * @param props The properties to import.
     */
    // Exposed for unit testing
    void importProperties(Properties props) {
        for (Map.Entry<Object,Object> e : props.entrySet()) {
            // These two calls use .toString rather than a cast
            // because sometimes people insert Integers into the system properties.
            String param = e.getKey().toString();
            String value = e.getValue() == null ? "null" : e.getValue().toString();
            // Checks to see if the system property could be a valid global property,
            // only insert it if it is.
            // Bypasses setValue as that throws an exception if the key isn't well formed.
            String testValue = "${" + param + "}";
            Matcher m = GlobalProperty.globalSymbolPattern.matcher(testValue);
            if (m.matches()) {
                map.put(param, new GlobalProperty(value));
            }
        }
    }

    /**
     * Adds a value to this GlobalProperties. Throws PropertyException if the
     * name does not conform to the {@link GlobalProperty#globalSymbolPattern}.
     * <p>
     * It overwrites values if they already exist.
     * @param propertyName The name of the new global property.
     * @param value The value for the new global property.
     * @throws PropertyException If the name is invalid.
     */
    public void setValue(String propertyName, String value) throws PropertyException {
        setValue(propertyName, new GlobalProperty(value));
    }

    /**
     * Adds a value to this GlobalProperties. Throws PropertyException if the
     * name does not conform to the {@link GlobalProperty#globalSymbolPattern}.
     * <p>
     * It overwrites values if they already exist.
     * @param propertyName The name of the new global property.
     * @param value The value for the new global property.
     * @throws PropertyException If the name is invalid.
     */
    public void setValue(String propertyName, GlobalProperty value) throws PropertyException {
        String testValue = "${" + propertyName + "}";
        Matcher m = GlobalProperty.globalSymbolPattern.matcher(testValue);
        if (!m.matches()) {
            throw new PropertyException("GlobalProperties",propertyName,"Does not conform to the GlobalProperty regex");
        }
        map.put(propertyName, value);
    }

    /**
     * Adds all the global properties from the other {@link GlobalProperties},
     * overwriting properties with the same name.
     * @param otherGP The global properties to add.
     */
    public void putAll(GlobalProperties otherGP) {
        for (Map.Entry<String,GlobalProperty> p : otherGP) {
            map.put(p.getKey(),p.getValue());
        }
    }

    /**
     * Removes the specified global property if it exists.
     * @param key The property name to remove.
     */
    public void remove(String key) {
        map.remove(key);
    }
}
