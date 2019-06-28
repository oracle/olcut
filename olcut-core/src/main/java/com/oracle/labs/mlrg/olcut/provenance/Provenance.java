package com.oracle.labs.mlrg.olcut.provenance;

import java.io.Serializable;

/**
 * A supertype for Provenance objects.
 *
 * One day it will be sealed, currently it is only
 * extended by {@link ListProvenance}, {@link MapProvenance},
 * {@link ObjectProvenance} and {@link PrimitiveProvenance}.
 *
 * Directly subclassing this will cause the serialisation mechanisms
 * for this package to throw ProvenanceException.
 */
public interface Provenance extends Serializable { }
