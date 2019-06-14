package com.oracle.labs.mlrg.olcut.provenance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Provenance for a list of provenancable types.
 */
public final class ListProvenance<T extends Provenance> implements Provenance, Iterable<T> {

    private final List<T> list;

    public ListProvenance(List<T> list) {
        this.list = Collections.unmodifiableList(new ArrayList<>(list));
    }

    /**
     * Creates an empty list provenance of the appropriate type.
     */
    public ListProvenance() {
        this.list = Collections.emptyList();
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListProvenance)) return false;
        ListProvenance<?> that = (ListProvenance<?>) o;
        return list.equals(that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    public static <T extends Provenance, U extends Provenancable<T>> ListProvenance<T> createListProvenance(Collection<U> collection) {
        if (collection == null || collection.isEmpty()) {
            return new ListProvenance<>();
        } else {
            List<T> outputList = new ArrayList<>();

            for (U e : collection) {
                outputList.add(e.getProvenance());
            }

            return new ListProvenance<>(outputList);
        }
    }
}
