package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a date/time value with time zone offset.
 */
public final class DateTimeProvenance implements PrimitiveProvenance<OffsetDateTime> {
    private static final long serialVersionUID = 1L;

    private final String key;

    private final OffsetDateTime value;

    public DateTimeProvenance(String key, OffsetDateTime value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public OffsetDateTime getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateTimeProvenance)) return false;
        DateTimeProvenance that = (DateTimeProvenance) o;
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
