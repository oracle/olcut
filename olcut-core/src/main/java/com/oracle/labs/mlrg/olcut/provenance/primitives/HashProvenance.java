package com.oracle.labs.mlrg.olcut.provenance.primitives;

import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil.HashType;

import java.util.Objects;

/**
 *
 */
public final class HashProvenance implements PrimitiveProvenance<String> {
    private static final long serialVersionUID = 1L;

    private final HashType type;

    private final String key;

    private final String value;

    public HashProvenance(HashType type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public HashProvenance(HashType type, String key, byte[] value) {
        this.type = type;
        this.key = key;
        this.value = ProvenanceUtil.bytesToHexString(value);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return type.name+"["+value+"]";
    }

    public HashType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashProvenance)) return false;
        HashProvenance that = (HashProvenance) o;
        return type == that.type &&
                key.equals(that.key) &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, key, value);
    }

    @Override
    public String toString() {
        return getValue();
    }
}
