package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records an enum value.
 */
public final class EnumProvenance<E extends Enum> implements PrimitiveProvenance<E> {
    private static final long serialVersionUID = 1L;

    private final String key;

    private final E value;

    private final String enumClass;

    public EnumProvenance(String key, E value) {
        this.key = key;
        this.value = value;
        this.enumClass = value.getClass().getName();
    }

    public String getEnumClass() {
        return enumClass;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public E getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnumProvenance)) return false;
        EnumProvenance that = (EnumProvenance) o;
        return key.equals(that.key) &&
                value.equals(that.value) &&
                enumClass.equals(that.enumClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, enumClass);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
