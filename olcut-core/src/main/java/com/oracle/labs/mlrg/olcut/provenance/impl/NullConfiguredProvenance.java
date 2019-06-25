package com.oracle.labs.mlrg.olcut.provenance.impl;

import com.oracle.labs.mlrg.olcut.provenance.ConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenancable;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;

import java.util.Collections;
import java.util.Map;

/**
 * A provenance object to use for null valued fields of {@link Provenancable} objects.
 */
public class NullConfiguredProvenance implements ConfiguredObjectProvenance {
    private static final long serialVersionUID = 1L;

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
        return className.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NullConfiguredProvenance) {
            return className.equals(((NullConfiguredProvenance) obj).className);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "null";
    }
}
