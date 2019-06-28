package com.oracle.labs.mlrg.olcut.provenance.impl;

import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A pile of reflection based magic used to automatically extract the values of configurable
 * fields. Supports all the types used by the configuration system, except for
 * Random as it's impossible to generate a true provenance for a {@link java.util.Random} instance.
 */
public final class ConfiguredObjectProvenanceImpl extends SkeletalConfiguredObjectProvenance {
    private static final long serialVersionUID = 1L;

    public <T extends Configurable> ConfiguredObjectProvenanceImpl(T host, String hostTypeStringName) {
        super(host,hostTypeStringName);
    }

    /**
     * Constructs a ConfiguredObjectProvenanceImpl from a provenance map.
     *
     * This constructor cannot verify that the map contains only configured parameters, as
     * that could trigger class loading of the host class. Thus it must contain at most
     * the configured parameters, {@link ObjectProvenance#CLASS_NAME}, and
     * {@link SkeletalConfiguredObjectProvenance#HOST_SHORT_NAME} provenances.
     * @param map The configured parameters map.
     */
    public ConfiguredObjectProvenanceImpl(Map<String,Provenance> map) {
        super(extractProvenanceInfo(map));
    }

    protected static ExtractedInfo extractProvenanceInfo(Map<String,Provenance> map) {
        String className;
        String hostTypeStringName;
        Map<String,Provenance> configuredParameters = new HashMap<>(map);
        if (configuredParameters.containsKey(ObjectProvenance.CLASS_NAME)) {
            className = configuredParameters.remove(ObjectProvenance.CLASS_NAME).toString();
        } else {
            throw new ProvenanceException("Failed to find class name when constructing ConfiguredObjectProvenanceImpl");
        }
        if (configuredParameters.containsKey(HOST_SHORT_NAME)) {
            hostTypeStringName = configuredParameters.remove(HOST_SHORT_NAME).toString();
        } else {
            throw new ProvenanceException("Failed to find host type short name when constructing ConfiguredObjectProvenanceImpl");
        }

        return new ExtractedInfo(className,hostTypeStringName,configuredParameters,Collections.emptyMap());
    }

}
