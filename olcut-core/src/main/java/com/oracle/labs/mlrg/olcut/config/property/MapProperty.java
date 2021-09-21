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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A MapProperty is a container for a Map from String to Property.
 */
public final class MapProperty implements Property {
    private static final long serialVersionUID = 1L;

    private final Map<String,SimpleProperty> map;

    public MapProperty(Map<String,SimpleProperty> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    public Map<String,SimpleProperty> getMap() {
        return map;
    }

    @Override
    public MapProperty copy() {
        Map<String,SimpleProperty> output = new HashMap<>();

        for (Map.Entry<String,SimpleProperty> e : map.entrySet()) {
            output.put(e.getKey(),e.getValue().copy());
        }

        return new MapProperty(output);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapProperty)) return false;
        MapProperty that = (MapProperty) o;
        return getMap().equals(that.getMap());
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public static MapProperty createFromStringMap(Map<String,String> input) {
        Map<String, SimpleProperty> output = new HashMap<>();

        for (Map.Entry<String,String> e : input.entrySet()) {
            output.put(e.getKey(),new SimpleProperty(e.getValue()));
        }

        return new MapProperty(output);
    }
}
