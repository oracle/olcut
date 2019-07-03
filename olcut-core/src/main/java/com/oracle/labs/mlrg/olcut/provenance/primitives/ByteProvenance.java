package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;

import java.util.Objects;

/**
 * A {@link PrimitiveProvenance} which records a Byte value.
 */
public final class ByteProvenance implements PrimitiveProvenance<Byte> {
    private static final long serialVersionUID = 1L;

    private final String key;

    private final byte value;

    public ByteProvenance(String key, byte value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Byte getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ByteProvenance)) return false;
        ByteProvenance that = (ByteProvenance) o;
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
