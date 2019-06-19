package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a date/time value.
 */
public final class DateTimeProvenance implements PrimitiveProvenance<LocalDateTime> {
    private static final long serialVersionUID = 1L;

    private final String key;

    private final LocalDateTime value;

    public DateTimeProvenance(String key, LocalDateTime value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public LocalDateTime getValue() {
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
