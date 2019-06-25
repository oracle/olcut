package com.oracle.labs.mlrg.olcut.provenance.io;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 *
 */
public final class ListMarshalledProvenance implements FlatMarshalledProvenance {
    private static final Logger logger = Logger.getLogger(ListMarshalledProvenance.class.getName());

    private final List<SimpleMarshalledProvenance> simpleList;

    public ListMarshalledProvenance(List<SimpleMarshalledProvenance> simpleList) {
        this.simpleList = Collections.unmodifiableList(simpleList);
    }

    public List<SimpleMarshalledProvenance> getSimpleList() {
        return simpleList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListMarshalledProvenance)) return false;
        ListMarshalledProvenance that = (ListMarshalledProvenance) o;
        return getSimpleList().equals(that.getSimpleList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSimpleList());
    }

    @Override
    public String toString() {
        return simpleList.toString();
    }
}
