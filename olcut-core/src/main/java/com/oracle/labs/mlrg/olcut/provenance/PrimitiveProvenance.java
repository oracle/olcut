package com.oracle.labs.mlrg.olcut.provenance;

/**
 * A supertype for {@link Provenance}s which are well understood
 * by the provenance system, and are expressible as a single {@link String}.
 * These classes form a sealed set and live in {@link com.oracle.labs.mlrg.olcut.provenance.primitives}.
 *
 * When adding a new PrimitiveProvenance the ProvenanceUtil and SimpleMarshalledProvenance
 * classes must also be updated to be taught about the new type.
 *
 * Must implement a public constructor which accepts a series of Strings, one
 * for the key, one which accepts the output of it's toString for
 * the value, along with other information if required.
 */
public interface PrimitiveProvenance<T> extends Provenance {

    /**
     * Gets the key associated with this provenance,
     * which is usually the field name associated with it's host
     * object.
     * @return The key.
     */
    public String getKey();

    /**
     * Gets the value of this provenance.
     * @return The value.
     */
    public T getValue();

}
