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

package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.provenance.primitives.FloatProvenance;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * A Provenance which is a map from {@link String} to other {@link Provenance}
 * objects.
 */
public final class MapProvenance<T extends Provenance> implements Provenance, Iterable<Pair<String,T>> {
    private static final long serialVersionUID = 1L;

    private final Map<String,T> map;

    /**
     * Creates a MapProvenance from a map. The map is defensively copied
     * and immutable.
     * @param map The map of provenances.
     */
    public MapProvenance(Map<String,T> map) {
        this.map = Collections.unmodifiableMap(new HashMap<>(map));
    }

    /**
     * Creates an empty MapProvenance.
     */
    public MapProvenance() {
        this.map = Collections.emptyMap();
    }

    /**
     * An unmodifiable view on the provenance map.
     * @return The provenance map.
     */
    public Map<String,T> getMap() {
        return map;
    }

    @Override
    public Iterator<Pair<String, T>> iterator() {
        return new MapProvenanceIterator<>(map.entrySet().iterator());
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapProvenance<?> that)) return false;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    /**
     * Creates a map provenance from a map of {@link Provenancable} objects by calling {@link Provenancable#getProvenance()}
     * on each element.
     * @param map The map of provenancable objects.
     * @param <T> The type of provenance emitted.
     * @param <U> The provenancable type.
     * @return A MapProvenance.
     */
    public static <T extends Provenance, U extends Provenancable<T>> MapProvenance<T> createMapProvenance(Map<String,U> map) {
        if (map == null || map.isEmpty()) {
            return new MapProvenance<>();
        } else {
            Map<String, T> outputMap = new HashMap<>();

            for (Map.Entry<String, U> e : map.entrySet()) {
                outputMap.put(e.getKey(), e.getValue().getProvenance());
            }

            return new MapProvenance<>(outputMap);
        }
    }

    /**
     * Creates a map provenance from a map of floats.
     * @param map The map of floats.
     * @return A MapProvenance.
     */
    public static MapProvenance<FloatProvenance> createMapProvenanceFromFloats(Map<String,Float> map) {
        if (map == null || map.isEmpty()) {
            return new MapProvenance<>();
        } else {
            Map<String, FloatProvenance> outputMap = new HashMap<>();

            for (Map.Entry<String, Float> e : map.entrySet()) {
                outputMap.put(e.getKey(), new FloatProvenance(e.getKey(),e.getValue()));
            }

            return new MapProvenance<>(outputMap);
        }
    }

    private static class MapProvenanceIterator<T extends Provenance> implements Iterator<Pair<String,T>> {

        private final Iterator<Map.Entry<String,T>> itr;

        public MapProvenanceIterator(Iterator<Map.Entry<String,T>> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Pair<String, T> next() {
            Map.Entry<String,T> item = itr.next();
            return new Pair<>(item.getKey(),item.getValue());
        }
    }

}
