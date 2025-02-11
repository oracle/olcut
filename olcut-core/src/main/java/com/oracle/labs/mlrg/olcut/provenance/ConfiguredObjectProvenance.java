/*
 * Copyright (c) 2019, 2025, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.provenance.impl.NullConfiguredProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.StringProvenance;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Provenance for a specific object known to the config system.
 * <p>
 * By convention all provenances which do not refer to an object field
 * use hyphens as separators. Provenances which refer to an object field
 * use standard Java camel case.
 */
public interface ConfiguredObjectProvenance extends ObjectProvenance {

    /**
     * Returns a (possibly nested) map describing the configuration parameters of this object.
     * @return The configuration parameters.
     */
    public Map<String,Provenance> getConfiguredParameters();

    /**
     * Returns a map containing the derived values describing the specific instance of
     * the object. For example the number of times it's RNG was used, the hash of any data
     * processed by the object etc.
     *
     * Defaults to returning {@link Collections#emptyMap}.
     * @return The instance values.
     */
    default public Map<String, PrimitiveProvenance<?>> getInstanceValues() {
        return Collections.emptyMap();
    }

    @Override
    default public Iterator<Pair<String, Provenance>> iterator() {
        ArrayList<Pair<String,Provenance>> iterable = new ArrayList<>();
        iterable.add(new Pair<>(CLASS_NAME,new StringProvenance(CLASS_NAME,getClassName())));
        for (Map.Entry<String,Provenance> m : getConfiguredParameters().entrySet()) {
            iterable.add(new Pair<>(m.getKey(),m.getValue()));
        }
        for (Map.Entry<String,PrimitiveProvenance<?>> m : getInstanceValues().entrySet()) {
            iterable.add(new Pair<>(m.getKey(),m.getValue()));
        }
        return Collections.unmodifiableList(iterable).iterator();
    }

    /**
     * Returns a new instance of the null provenance, used when fields of a
     * configured object are null.
     * @param className The class name of the host object.
     * @return A null provenance.
     */
    public static ConfiguredObjectProvenance getEmptyProvenance(String className) {
        return new NullConfiguredProvenance(className);
    }
}
