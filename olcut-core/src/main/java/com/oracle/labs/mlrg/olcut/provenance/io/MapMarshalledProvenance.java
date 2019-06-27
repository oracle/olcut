package com.oracle.labs.mlrg.olcut.provenance.io;

import com.oracle.labs.mlrg.olcut.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 *
 */
public final class MapMarshalledProvenance implements FlatMarshalledProvenance, Iterable<Pair<String,FlatMarshalledProvenance>> {

    private final Map<String, FlatMarshalledProvenance> map;

    public MapMarshalledProvenance(Map<String, FlatMarshalledProvenance> map) {
        this.map = Collections.unmodifiableMap(new HashMap<>(map));
    }

    public MapMarshalledProvenance() {
        this.map = Collections.emptyMap();
    }

    @Override
    public Iterator<Pair<String, FlatMarshalledProvenance>> iterator() {
        return new MapMarshalledProvenanceIterator(map.entrySet().iterator());
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapMarshalledProvenance)) return false;
        MapMarshalledProvenance that = (MapMarshalledProvenance) o;
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
