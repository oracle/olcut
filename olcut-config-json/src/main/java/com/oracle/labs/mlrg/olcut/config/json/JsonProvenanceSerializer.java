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

package com.oracle.labs.mlrg.olcut.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.oracle.labs.mlrg.olcut.provenance.io.FlatMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ListMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.MapMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.MarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.SimpleMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.io.IOException;
import java.util.Map;

/**
 * Serialization class to convert {@link MarshalledProvenance} into JSON.
 */
public class JsonProvenanceSerializer extends StdSerializer<MarshalledProvenance> {

    public JsonProvenanceSerializer(Class<MarshalledProvenance> provClass) {
        super(provClass);
    }

    @Override
    public void serialize(MarshalledProvenance marshalledProvenance, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Class<?> provClass = marshalledProvenance.getClass();
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(JsonProvenanceModule.MARSHALLED_CLASS,provClass.getName());
        if (marshalledProvenance instanceof ListMarshalledProvenance lmp) {
            jsonGenerator.writeArrayFieldStart(JsonProvenanceModule.LIST);
            for (FlatMarshalledProvenance e : lmp) {
                jsonGenerator.writeObject(e);
            }
            jsonGenerator.writeEndArray();
        } else if (marshalledProvenance instanceof MapMarshalledProvenance mmp) {
            jsonGenerator.writeObjectFieldStart(JsonProvenanceModule.MAP);
            for (Pair<String, FlatMarshalledProvenance> e : mmp) {
                jsonGenerator.writeObjectField(e.getA(),e.getB());
            }
            jsonGenerator.writeEndObject();
        } else if (marshalledProvenance instanceof ObjectMarshalledProvenance omp) {
            jsonGenerator.writeStringField(JsonProvenanceModule.OBJECT_NAME,omp.name());
            jsonGenerator.writeStringField(JsonProvenanceModule.OBJECT_CLASS_NAME,omp.objectClassName());
            jsonGenerator.writeStringField(JsonProvenanceModule.PROVENANCE_CLASS,omp.provenanceClassName());
            jsonGenerator.writeObjectFieldStart(JsonProvenanceModule.MAP);
            for (Map.Entry<String, FlatMarshalledProvenance> e : omp.map().entrySet()) {
                jsonGenerator.writeObjectField(e.getKey(),e.getValue());
            }
            jsonGenerator.writeEndObject();
        } else if (marshalledProvenance instanceof SimpleMarshalledProvenance smp) {
            jsonGenerator.writeStringField(JsonProvenanceModule.KEY,smp.getKey());
            jsonGenerator.writeStringField(JsonProvenanceModule.VALUE,smp.getValue());
            jsonGenerator.writeStringField(JsonProvenanceModule.PROVENANCE_CLASS,smp.getProvenanceClassName());
            jsonGenerator.writeStringField(JsonProvenanceModule.ADDITIONAL,smp.getAdditional());
            jsonGenerator.writeBooleanField(JsonProvenanceModule.IS_REFERENCE,smp.isReference());
        } else {
            throw new IOException("Unexpected provenance class, found " + provClass.getName());
        }
        jsonGenerator.writeEndObject();
    }
}
