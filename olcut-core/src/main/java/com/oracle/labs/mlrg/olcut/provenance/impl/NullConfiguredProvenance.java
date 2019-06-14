package com.oracle.labs.mlrg.olcut.provenance.impl;

import com.oracle.labs.mlrg.olcut.provenance.ConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;

import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class NullConfiguredProvenance implements ConfiguredObjectProvenance {
    private final String className;

    public NullConfiguredProvenance(String className) {
        this.className = className;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Map<String, Provenance> getConfiguredParameters() {
        return Collections.emptyMap();
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullConfiguredProvenance;
    }

    @Override
    public String toString() {
        return "null";
    }
}
