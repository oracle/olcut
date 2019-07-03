package com.oracle.labs.mlrg.olcut.provenance.io;

/**
 * A marshalled provenance that is either a {@link ListMarshalledProvenance},
 * a {@link MapMarshalledProvenance} or a {@link SimpleMarshalledProvenance}.
 *
 * Will be sealed to those types one day.
 */
public interface FlatMarshalledProvenance extends MarshalledProvenance { }
