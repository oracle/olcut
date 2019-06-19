package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a Float value.
 */
public final class FloatProvenance implements PrimitiveProvenance<Float> {
    private static final long serialVersionUID = 1L;

    private final String key;

    private final float value;

    public FloatProvenance(String key, float value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FloatProvenance)) return false;
        FloatProvenance that = (FloatProvenance) o;
        return key.equals(that.key) &&
                Float.compare(value,that.value) == 0;
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
