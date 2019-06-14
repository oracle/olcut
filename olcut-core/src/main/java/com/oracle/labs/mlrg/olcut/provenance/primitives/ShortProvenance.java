package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a Short value.
 */
public final class ShortProvenance implements PrimitiveProvenance<Short> {

    private final String key;

    private final short value;

    public ShortProvenance(String key, short value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Short getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortProvenance)) return false;
        ShortProvenance that = (ShortProvenance) o;
        return key.equals(that.key) &&
                value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return ""+value;
    }
}
