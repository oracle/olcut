package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.time.OffsetTime;
import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a time value with time zone offset.
 */
public final class TimeProvenance implements PrimitiveProvenance<OffsetTime> {
    private static final long serialVersionUID = 1L;

    private final String key;

    private final OffsetTime value;

    public TimeProvenance(String key, OffsetTime value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public OffsetTime getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeProvenance)) return false;
        TimeProvenance that = (TimeProvenance) o;
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
