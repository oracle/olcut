/*
 * Copyright (c) 2004-2021, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.provenance.io;

import com.oracle.labs.mlrg.olcut.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * A marshalled provenance which contains a map of other {@link FlatMarshalledProvenance} objects.
 * This can recursively include other lists or maps.
 */
public final class MapMarshalledProvenance implements FlatMarshalledProvenance, Iterable<Pair<String,FlatMarshalledProvenance>> {

    private final Map<String, FlatMarshalledProvenance> map;

    /**
     * Constructs a MapMarshalledProvenance wrapped around the supplied map.
     * @param map The map.
     */
    public MapMarshalledProvenance(Map<String, FlatMarshalledProvenance> map) {
        this.map = Collections.unmodifiableMap(new HashMap<>(map));
    }

    /**
     * Constructs an empty MapMarshalledProvenance.
     */
    public MapMarshalledProvenance() {
        this.map = Collections.emptyMap();
    }

    /**
     * Is this MapMarshalledProvenance empty?
     * @return True if the provenance contains no other provenances.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Iterator<Pair<String, FlatMarshalledProvenance>> iterator() {
        return new MapMarshalledProvenanceIterator(map.entrySet().iterator());
    }

    @Override
    public String toString() {
        return "MapMarshalledProvenance" + map.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapMarshalledProvenance that)) return false;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    private static class MapMarshalledProvenanceIterator implements Iterator<Pair<String, FlatMarshalledProvenance>> {

        private final Iterator<Entry<String, FlatMarshalledProvenance>> itr;

        public MapMarshalledProvenanceIterator(Iterator<Map.Entry<String, FlatMarshalledProvenance>> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Pair<String, FlatMarshalledProvenance> next() {
            Map.Entry<String, FlatMarshalledProvenance> item = itr.next();
            return new Pair<>(item.getKey(),item.getValue());
        }
    }

}
