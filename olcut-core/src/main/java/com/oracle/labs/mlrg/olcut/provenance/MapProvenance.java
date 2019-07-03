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
        if (!(o instanceof MapProvenance)) return false;
        MapProvenance<?> that = (MapProvenance<?>) o;
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
