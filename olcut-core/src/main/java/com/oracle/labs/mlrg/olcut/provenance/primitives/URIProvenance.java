package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.net.URI;
import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a resource location.
 */
public final class URIProvenance implements PrimitiveProvenance<URI> {
    private static final long serialVersionUID = 1L;

    private final String key;

    private final URI value;

    public URIProvenance(String key, URI value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public URI getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof URIProvenance)) return false;
        URIProvenance that = (URIProvenance) o;
        return key.equals(that.key) &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
