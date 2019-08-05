package com.oracle.labs.mlrg.olcut.provenance.io;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A marshalled provenance which contains a list of other {@link FlatMarshalledProvenance} objects.
 * This can recursively include other lists or maps.
 */
public final class ListMarshalledProvenance implements FlatMarshalledProvenance, Iterable<FlatMarshalledProvenance> {
    private static final Logger logger = Logger.getLogger(ListMarshalledProvenance.class.getName());

    private final List<FlatMarshalledProvenance> list;

    public ListMarshalledProvenance(List<FlatMarshalledProvenance> list) {
        this.list = Collections.unmodifiableList(list);
    }

    /**
     * Gets an unmodifiable view on the marshalled provenance list.
     * @return The marshalled provenance list.
     */
    public List<FlatMarshalledProvenance> getList() {
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListMarshalledProvenance)) return false;
        ListMarshalledProvenance that = (ListMarshalledProvenance) o;
        return getList().equals(that.getList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getList());
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public Iterator<FlatMarshalledProvenance> iterator() {
        return list.iterator();
    }
}
