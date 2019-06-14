package com.oracle.labs.mlrg.olcut.provenance;

/**
 *
 */
public interface PrimitiveProvenance<T> extends Provenance {

    public String getKey();

    public T getValue();

}
