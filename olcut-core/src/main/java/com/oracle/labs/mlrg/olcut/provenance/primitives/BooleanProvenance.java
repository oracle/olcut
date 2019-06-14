package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a Boolean value.
 */
public final class BooleanProvenance implements PrimitiveProvenance<Boolean> {

    private final String key;

    private final boolean value;

    public BooleanProvenance(String key, boolean value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BooleanProvenance)) return false;
        BooleanProvenance that = (BooleanProvenance) o;
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
