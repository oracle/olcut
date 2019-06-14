package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a Double value.
 */
public final class DoubleProvenance implements PrimitiveProvenance<Double> {

    private final String key;

    private final double value;

    public DoubleProvenance(String key, double value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoubleProvenance)) return false;
        DoubleProvenance that = (DoubleProvenance) o;
        return key.equals(that.key) &&
                Double.compare(value,that.value) == 0;
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
