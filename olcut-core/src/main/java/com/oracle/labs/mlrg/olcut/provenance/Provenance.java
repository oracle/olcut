package com.oracle.labs.mlrg.olcut.provenance;

import java.io.Serializable;

/**
 * A supertype for Provenance objects.
 *
 * One day it will be sealed, currently it should
 * only be extended by {@link PrimitiveProvenance}, {@link ListProvenance},
 * {@link MapProvenance}, and {@link ObjectProvenance}.
 *
 * Directly subclassing this is likely to break the serialisation mechanisms
 * for this package.
 */
public interface Provenance extends Serializable { }
