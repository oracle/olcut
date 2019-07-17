package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a date value.
 */
public final class DateProvenance implements PrimitiveProvenance<LocalDate> {
    private static final long serialVersionUID = 1L;

    private final String key;

    private final LocalDate value;

    public DateProvenance(String key, LocalDate value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public LocalDate getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateProvenance)) return false;
        DateProvenance that = (DateProvenance) o;
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
