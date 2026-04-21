/*
 * Copyright (c) 2019-2025, Oracle and/or its affiliates.
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

import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil.HashType;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.util.Map;
import java.util.Optional;

/**
 * A provenance object which records object fields.
 * <p>
 * Must record the class name of the host object so it can be recovered.
 * <p>
 * All classes which implement this interface must expose a public constructor
 * which accepts a Map&lt;String,Provenance&gt; which is used in deserialisation,
 * and have consistent implementations of equals and hashCode.
 * <p>
 * By convention all provenances which do not refer to an object field
 * use hyphens as separators for their name. Provenances which refer to an object field
 * use standard Java camel case.
 */
public non-sealed interface ObjectProvenance extends Provenance, Iterable<Pair<String,Provenance>> {

    public static final String CLASS_NAME = "class-name";
    public static final HashType DEFAULT_HASH_TYPE = HashType.SHA256;

    /**
     * Returns the class name of the object which produced this ObjectProvenance instance.
     * @return The class name.
     */
    public String getClassName();

    /**
     * Generates a String representation of this provenance.
     * <p>
     * Commonly used to implement toString.
     * @param name The name to give the provenance.
     * @return A string representation.
     */
    default public String generateString(String name) {
        StringBuilder sb = new StringBuilder();

        sb.append(name);
        sb.append("(");
        for (Pair<String,Provenance> p : this) {
            sb.append(p.a());
            sb.append('=');
            sb.append(p.b().toString());
            sb.append(',');
        }
        sb.replace(sb.length()-1,sb.length(),")");

        return sb.toString();
    }

    /**
     * Removes the specified Provenance from the supplied map and returns it. Checks that it's the right type,
     * and casts to it before returning.
     * <p>
     * Throws ProvenanceException if it's not found or it's an incorrect type.
     * @param map The map to check.
     * @param key The key to look up.
     * @param type The type to check the value against.
     * @param provClassName The name of the requesting class (to ensure the exception has the appropriate error message).
     * @param <T> The type of the value.
     * @return The specified provenance object.
     * @throws ProvenanceException if the key is not found, or the value is not the requested type.
     */
    public static <T extends Provenance> T checkAndExtractProvenance(Map<String,Provenance> map, String key, Class<T> type, String provClassName) throws ProvenanceException {
        Optional<T> prov = maybeExtractProvenance(map,key,type,provClassName);
        if (prov.isPresent()) {
            return prov.get();
        } else {
            throw new ProvenanceException("Failed to find " + key + " when constructing " + provClassName);
        }
    }

    /**
     * Removes the specified Provenance from the supplied map and returns it. Checks that it's the right type,
     * and casts to it before returning. Unlike {@link #checkAndExtractProvenance(Map, String, Class, String)} it doesn't
     * throw if it fails to find the key, only if the value is of the wrong type.
     * <p>
     * This is used when evolving provenance classes by adding new fields to ensure that old serialized
     * forms remain compatible.
     * @param map The map to inspect.
     * @param key The key to find.
     * @param type The class of the value.
     * @param provClassName The name of the requesting class (to ensure the exception has the appropriate error message).
     * @param <T> The type of the value.
     * @return An optional containing the value if present.
     * @throws ProvenanceException If the value is the wrong type.
     */
    @SuppressWarnings("unchecked") // Guarded by isInstance check
    public static <T extends Provenance> Optional<T> maybeExtractProvenance(Map<String,Provenance> map, String key, Class<T> type, String provClassName) throws ProvenanceException {
        Provenance tmp = map.remove(key);
        if (tmp != null) {
            if (type.isInstance(tmp)) {
                return Optional.of((T) tmp);
            } else {
                throw new ProvenanceException("Failed to cast " + key + " when constructing " + provClassName + ", found " + tmp);
            }
        } else {
            return Optional.empty();
        }
    }
}
