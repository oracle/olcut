package com.oracle.labs.mlrg.olcut.provenance.io;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.util.Objects;

/**
 *
 */
public final class SimpleMarshalledProvenance implements FlatMarshalledProvenance {

    private final String key;

    private final String value;

    private final String provenanceClassName;

    public SimpleMarshalledProvenance(String key, String value, String provenanceClassName) {
        this.key = key;
        this.value = value;
        this.provenanceClassName = provenanceClassName;
    }

    public <T> SimpleMarshalledProvenance(PrimitiveProvenance<T> provenance) {
        this.key = provenance.getKey();
        this.value = provenance.getValue().toString();
        this.provenanceClassName = provenance.getClass().getName();
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getProvenanceClassName() {
        return provenanceClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleMarshalledProvenance)) return false;
        SimpleMarshalledProvenance that = (SimpleMarshalledProvenance) o;
        return key.equals(that.key) &&
                value.equals(that.value) &&
                provenanceClassName.equals(that.provenanceClassName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, provenanceClassName);
    }
}
