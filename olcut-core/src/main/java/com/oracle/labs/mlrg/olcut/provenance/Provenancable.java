package com.oracle.labs.mlrg.olcut.provenance;

/**
 *
 */
public interface Provenancable<T extends Provenance> {

    public T getProvenance();

}
