package com.oracle.labs.mlrg.olcut.provenance;

/**
 * A wrapper exception for things thrown during marshalling and unmarshalling
 * of {@link Provenance} objects.
 */
public class ProvenanceException extends RuntimeException {

    public ProvenanceException(String message) {
        super(message);
    }

    public ProvenanceException(String message, Throwable cause) {
        super(message,cause);
    }

}
