package com.oracle.labs.mlrg.olcut.provenance;

/**
 * A supertype for {@link Provenance}s which are well understood
 * by the provenance system, and are expressible as a single {@link String}.
 *
 * Must implement a public constructor which accepts two Strings, one
 * for the key and one which accepts the output of it's toString for
 * the value.
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
