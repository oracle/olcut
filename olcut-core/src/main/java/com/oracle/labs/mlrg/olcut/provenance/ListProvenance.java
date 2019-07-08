package com.oracle.labs.mlrg.olcut.provenance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Provenance for a list of provenance objects.
 */
public final class ListProvenance<T extends Provenance> implements Provenance, Iterable<T> {
    private static final long serialVersionUID = 1L;

    private final List<T> list;

    /**
     * Creates a ListProvenance from the supplied list. The
     * list is defensively copied and immutable.
     * @param list The input list.
     */
    public ListProvenance(List<T> list) {
        this.list = Collections.unmodifiableList(new ArrayList<>(list));
    }

    /**
     * Creates an empty list provenance of the appropriate type.
     */
    public ListProvenance() {
        this.list = Collections.emptyList();
    }

    /**
     * An unmodifiable view on the provenance list.
     * @return The provenance list.
     */
    public List<T> getList() {
        return list;
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

    /**
     * Creates a list provenance from a {@link Collection} of objects which
     * implement {@link Provenancable}.
     * @param collection The collection to extract provenance from.
     * @param <T> The type of the provenance.
     * @param <U> The provenancable type of the collection.
     * @return A ListProvenance containing the provenances from the collection.
     */
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
