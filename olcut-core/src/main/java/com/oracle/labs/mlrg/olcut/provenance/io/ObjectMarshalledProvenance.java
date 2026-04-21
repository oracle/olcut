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

package com.oracle.labs.mlrg.olcut.provenance.io;

import java.util.Map;

/**
 * A marshalled provenance representing an
 * {@link com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance} subclass.
 * <p>
 * Contains the name of the object, the class name of the provenance's host object,
 * and the class name of the provenance object.
 * @param objectName          The name of the object in the provenance stream.
 * @param map                 The object's fields.
 * @param objectClassName     The class name of the {@link com.oracle.labs.mlrg.olcut.provenance.Provenancable} object.
 * @param provenanceClassName The class name of the {@link com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance} subclass.
 */
public record ObjectMarshalledProvenance(String objectName, Map<String, FlatMarshalledProvenance> map,
                                         String objectClassName, String provenanceClassName) implements MarshalledProvenance {

    /**
     * Constructs an ObjectMarshalledProvenance.
     *
     * @param objectName          The name of the object in the provenance stream.
     * @param map                 The object's fields.
     * @param objectClassName     The class name of the {@link com.oracle.labs.mlrg.olcut.provenance.Provenancable} object.
     * @param provenanceClassName The class name of the {@link com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance} subclass.
     */
    public ObjectMarshalledProvenance(String objectName, Map<String, FlatMarshalledProvenance> map, String objectClassName, String provenanceClassName) {
        this.objectName = objectName;
        this.map = Map.copyOf(map);
        this.objectClassName = objectClassName;
        this.provenanceClassName = provenanceClassName;
    }

    @Override
    public String toString() {
        return "ObjectMarshalledProvenance{" +
                "map=" + map +
                ", objectName='" + objectName + '\'' +
                ", objectClassName='" + objectClassName + '\'' +
                ", provenanceClassName='" + provenanceClassName + '\'' +
                '}';
    }
}
