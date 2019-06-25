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
public final class MapMarshalledProvenance implements FlatMarshalledProvenance {

    private final Map<String, SimpleMarshalledProvenance> map;

    public MapMarshalledProvenance(Map<String, SimpleMarshalledProvenance> map) {
        this.map = Collections.unmodifiableMap(new HashMap<>(map));
    }

    public MapMarshalledProvenance() {
        this.map = Collections.emptyMap();
    }

    public Iterator<Pair<String, SimpleMarshalledProvenance>> iterator() {
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

    private static class MapMarshalledProvenanceIterator implements Iterator<Pair<String, SimpleMarshalledProvenance>> {

        private final Iterator<Entry<String, SimpleMarshalledProvenance>> itr;

        public MapMarshalledProvenanceIterator(Iterator<Map.Entry<String, SimpleMarshalledProvenance>> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Pair<String, SimpleMarshalledProvenance> next() {
            Map.Entry<String, SimpleMarshalledProvenance> item = itr.next();
            return new Pair<>(item.getKey(),item.getValue());
        }
    }


}
