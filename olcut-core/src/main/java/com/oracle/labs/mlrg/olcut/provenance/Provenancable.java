package com.oracle.labs.mlrg.olcut.provenance;

/**
 * An interface which provides a method for generating {@link Provenance}
 * for the implementing object.
 */
public interface Provenancable<T extends Provenance> {

    /**
     * Returns the provenance of this object.
     * @return The provenance.
     */
    public T getProvenance();

}
