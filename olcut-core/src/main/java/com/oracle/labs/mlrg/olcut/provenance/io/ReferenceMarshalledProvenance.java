package com.oracle.labs.mlrg.olcut.provenance.io;

/**
 *
 */
public final class ReferenceMarshalledProvenance implements MarshalledProvenance {

    private final String name;

    private final String provenanceClassName;

    public ReferenceMarshalledProvenance(String name, String provenanceClassName) {
        this.name = name;
        this.provenanceClassName = provenanceClassName;
    }

    public String getName() {
        return name;
    }

    public String getProvenanceClassName() {
        return provenanceClassName;
    }

}
