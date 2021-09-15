/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oracle.labs.mlrg.olcut.provenance.io.MarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ProvenanceSerialization;
import com.oracle.labs.mlrg.olcut.provenance.io.ProvenanceSerializationException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for serializing and deserializing provenances to/from json.
 */
public final class JsonProvenanceSerialization implements ProvenanceSerialization {

    private static final TypeReference<List<MarshalledProvenance>> typeRef = new TypeReference<List<MarshalledProvenance>>() {};

    private final ObjectMapper mapper;

    /**
     * Construct a JsonProvenanceSerialization.
     *
     * @param indentOutput Indent the output.
     */
    public JsonProvenanceSerialization(boolean indentOutput) {
        mapper = new ObjectMapper();
        mapper.registerModule(new JsonProvenanceModule());
        if (indentOutput) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
    }

    @Override
    public String getFileExtension() {
        return "json";
    }

    @Override
    public List<ObjectMarshalledProvenance> deserializeFromFile(Path path) throws ProvenanceSerializationException, IOException {
        try {
            List<MarshalledProvenance> jsonProvenances = mapper.readValue(path.toFile(), typeRef);
            return convertMarshalledProvenanceList(jsonProvenances);
        } catch (JsonParseException | JsonMappingException e) {
            throw new ProvenanceSerializationException("Failed to parse JSON",e);
        }
    }

    @Override
    public List<ObjectMarshalledProvenance> deserializeFromString(String input) throws ProvenanceSerializationException {
        try {
            List<MarshalledProvenance> jsonProvenances = mapper.readValue(input, typeRef);
            return convertMarshalledProvenanceList(jsonProvenances);
        } catch (JsonProcessingException e) {
            throw new ProvenanceSerializationException("Failed to deserialize provenance", e);
        }
    }

    /**
     * Converts the list of {@link MarshalledProvenance}s to a list of {@link ObjectMarshalledProvenance}s.
     * <p>
     * This is because Jackson's deserialization doesn't give a sharp enough type, so we have to check.
     * It will throw {@link IllegalArgumentException} in the event the provenance stream was malformed and
     * so contained a top level {@link com.oracle.labs.mlrg.olcut.provenance.io.FlatMarshalledProvenance}.
     * @param provenances The provenances to cast.
     * @return A list of {@link ObjectMarshalledProvenance}s.
     */
    private static List<ObjectMarshalledProvenance> convertMarshalledProvenanceList(List<MarshalledProvenance> provenances) {
        List<ObjectMarshalledProvenance> jps = new ArrayList<>();
        for (MarshalledProvenance mp : provenances) {
            if (mp instanceof ObjectMarshalledProvenance) {
                jps.add((ObjectMarshalledProvenance) mp);
            } else {
                throw new IllegalArgumentException("Invalid provenance found, expected ObjectMarshalledProvenance, found " + mp);
            }
        }
        return jps;
    }

    @Override
    public String serializeToString(List<ObjectMarshalledProvenance> marshalledProvenances) {
        try {
            return mapper.writeValueAsString(marshalledProvenances);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize provenance", e);
        }
    }

    @Override
    public void serializeToFile(List<ObjectMarshalledProvenance> marshalledProvenances, Path path) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path.toFile())))) {
            writer.println(serializeToString(marshalledProvenances));
        }
    }
}
