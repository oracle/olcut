package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.net.URL;
import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a resource location.
 */
public final class URLProvenance implements PrimitiveProvenance<URL> {
    private static final long serialVersionUID = 1L;

    private final String key;

    private final URL value;

    public URLProvenance(String key, URL value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public URL getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof URLProvenance)) return false;
        URLProvenance that = (URLProvenance) o;
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
