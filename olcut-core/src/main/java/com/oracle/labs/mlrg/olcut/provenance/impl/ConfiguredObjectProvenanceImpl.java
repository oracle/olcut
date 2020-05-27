/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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

    /**
     * Constructs a ConfiguredObjectProvenanceImpl from the host object by inspecting it's
     * fields with reflection.
     * @param host The object to create Provenance from.
     * @param hostTypeStringName The type name to use in this provenance's toString.
     * @param <T> The type of the host object.
     */
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

    /**
     * Extracts the class name and host short name provenances. Assumes the rest of the
     * map entries are configured parameters.
     * @param map The Map of provenance entries.
     * @return An extracted info object with the class name and host short name parsed out.
     */
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
