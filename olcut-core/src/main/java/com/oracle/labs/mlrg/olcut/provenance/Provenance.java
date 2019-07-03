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
 *
 * Provenance implementations must override {@link Object#equals},
 * {@link Object#hashCode} and {@link Object#toString} to ensure
 * correct operation.
 *
 * Provenance implementations should be immutable.
 */
public interface Provenance extends Serializable { }
