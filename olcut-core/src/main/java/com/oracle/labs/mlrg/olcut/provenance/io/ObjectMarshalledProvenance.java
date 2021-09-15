/*
 * Copyright (c) 2004-2021, Oracle and/or its affiliates.
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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * A marshalled provenance representing an
 * {@link com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance} subclass.
 * <p>
 * Contains the name of the object, the class name of the provenance's host object,
 * and the class name of the provenance object.
 */
public final class ObjectMarshalledProvenance implements MarshalledProvenance {

    private final Map<String,FlatMarshalledProvenance> map;

    private final String objectName;

    private final String objectClassName;

    private final String provenanceClassName;

    /**
     * Constructs an ObjectMarshalledProvenance.
     * @param objectName The name of the object in the provenance stream.
     * @param map The object's fields.
     * @param objectClassName The class name of the {@link com.oracle.labs.mlrg.olcut.provenance.Provenancable} object.
     * @param provenanceClassName The class name of the {@link com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance} subclass.
     */
    public ObjectMarshalledProvenance(String objectName, Map<String, FlatMarshalledProvenance> map, String objectClassName, String provenanceClassName) {
        this.objectName = objectName;
        this.map = Collections.unmodifiableMap(map);
        this.objectClassName = objectClassName;
        this.provenanceClassName = provenanceClassName;
    }

    /**
     * Gets the object name.
     * @return The object name.
     */
    public String getName() {
        return objectName;
    }

    /**
     * The fields of the provenance.
     * @return The provenance fields.
     */
    public Map<String, FlatMarshalledProvenance> getMap() {
        return map;
    }

    /**
     * The class name of the {@link com.oracle.labs.mlrg.olcut.provenance.Provenancable} object.
     * @return The creator class of the provenance.
     */
    public String getObjectClassName() {
        return objectClassName;
    }

    /**
     * The class name of the {@link com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance} subclass.
     * @return The provenance class name.
     */
    public String getProvenanceClassName() {
        return provenanceClassName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectMarshalledProvenance)) return false;
        ObjectMarshalledProvenance that = (ObjectMarshalledProvenance) o;
        return map.equals(that.map) &&
                objectName.equals(that.objectName) &&
                objectClassName.equals(that.objectClassName) &&
                provenanceClassName.equals(that.provenanceClassName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, objectName, objectClassName, provenanceClassName);
    }
}
