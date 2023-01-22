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

import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * A collection of GlobalProperties which can't be mutated.
 * <p>
 * In addition to the set properties, it also includes
 * preset properties. This set currently includes {@link #HOSTNAME}
 * which lazily resolves to the system hostname.
 */
public class ImmutableGlobalProperties implements Iterable<Map.Entry<String,GlobalProperty>> {

    public static final String HOSTNAME = "gp.hostName";

    /**
     * A set of distinguished properties that we would like to have.
     */
    private static final Map<String, GlobalProperty> distinguished = new HashMap<>();

    protected final HashMap<String, GlobalProperty> map;

    static {
        distinguished.put(HOSTNAME, new LazyGlobalProperty(Util::getHostName));
    }

    /**
     * Creates an ImmutableGlobalProperties.
     */
    public ImmutableGlobalProperties() {
        this.map = new HashMap<>();
    }

    /**
     * Creates an ImmutableGlobalProperties copy of the other properties.
     * @param globalProperties The properties to copy.
     */
    public ImmutableGlobalProperties(GlobalProperties globalProperties) {
        this.map = new HashMap<>();
        for(String key : globalProperties.map.keySet()) {
            map.put(key, new GlobalProperty(globalProperties.get(key)));
        }
    }

    /**
     * Creates an ImmutableGlobalProperties view of this map.
     * @param map The map to wrap.
     */
    private ImmutableGlobalProperties(HashMap<String, GlobalProperty> map) {
        this.map = map;
    }

    /**
     * Returns the GlobalProperty associated with the name, or
     * null if there is no such property.
     * @param propertyName The property name.
     * @return The GlobalProperty if found, or null.
     */
    public GlobalProperty get(String propertyName) {
        GlobalProperty gp = map.get(propertyName);
        if(gp == null) {
            gp = distinguished.get(propertyName);
        }
        return gp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableGlobalProperties entries)) return false;
        return map.equals(entries.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    /**
     * Replaces all of the global properties in a property value with the appropriate
     * global values.
     * @param instanceName the name of the instance whose properties we're
     * processing.
     * @param propName the name of the property whose value we're processing
     * @param val the property value
     * @return the property value with all global properties replaced with their
     * corresponding values.
     */
    public String replaceGlobalProperties(String instanceName,
                                             String propName, String val) {
        Matcher m = GlobalProperty.globalSymbolPattern.matcher(val);
        boolean matched = false;
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            matched = true;
            //
            // Get the recursive replacement for this value.
            GlobalProperty prop = get(m.group(1));
            String replace = prop == null ? null : prop.getValue();
            if(replace == null) {
                throw new PropertyException(instanceName, propName,
                        "Unknown global property:  " +
                                m.group(0));
            }

            m.appendReplacement(sb, Matcher.quoteReplacement(replace));
        }
        m.appendTail(sb);
        if (matched) {
            return replaceGlobalProperties(instanceName,propName,sb.toString());
        } else {
            return sb.toString();
        }
    }

    /**
     * Returns a view over the property names.
     * @return The set of property names.
     */
    public Set<String> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * Returns an unmodifiable view of this GlobalProperties.
     * <p>
     * Changes in the underlying properties are reflected in this view.
     * @return An unmodifiable view of the properties.
     */
    public ImmutableGlobalProperties getImmutableProperties() {
        return new ImmutableGlobalProperties(map);
    }

    @Override
    public Iterator<Map.Entry<String, GlobalProperty>> iterator() {
        return map.entrySet().iterator();
    }

}
