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

package com.oracle.labs.mlrg.olcut.provenance.io;

import com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for serializing and deserializing marshalled provenances to
 * and from strings or files.
 */
public interface ProvenanceMarshaller {

    /**
     * The file extension this ProvenanceMarshaller supports.
     * @return The file extension.
     */
    public String getFileExtension();

    /**
     * Loads in a list of marshalled provenances from the specified file.
     * @param path The file to load.
     * @return The marshalled provenances in the file.
     * @throws ProvenanceSerializationException If the provenance could not be parsed from the file.
     * @throws IOException If the file failed to read.
     */
    public List<ObjectMarshalledProvenance> deserializeFromFile(Path path) throws ProvenanceSerializationException, IOException;

    /**
     * Loads in a list of marshalled provenances from the supplied string.
     * @param input The string to deserialize.
     * @return The marshalled provenances in the string.
     * @throws ProvenanceSerializationException If the provenance could not be parsed from the string.
     */
    public List<ObjectMarshalledProvenance> deserializeFromString(String input) throws ProvenanceSerializationException;

    /**
     * Deserializes and unmarshalls the provenances in the specified file.
     * @param path The file to load.
     * @return The object provenance specified by the marshalled provenances in the file.
     * @throws ProvenanceSerializationException If the provenance could not be parsed from the file.
     * @throws IOException If the file failed to read.
     */
    default public ObjectProvenance deserializeAndUnmarshal(Path path) throws ProvenanceSerializationException, IOException {
        return ProvenanceUtil.unmarshalProvenance(deserializeFromFile(path));
    }

    /**
     * Deserializes and unmarshalls the provenances from the supplied string..
     * @param input The string to deserialize.
     * @return The object provenance specified by the marshalled provenances in the string.
     * @throws ProvenanceSerializationException If the provenance could not be parsed from the string.
     */
    default public ObjectProvenance deserializeAndUnmarshal(String input) throws ProvenanceSerializationException {
        return ProvenanceUtil.unmarshalProvenance(deserializeFromString(input));
    }

    /**
     * Serializes the list of marshalled provenance to a string.
     * @param marshalledProvenances The provenances to serialize.
     * @return A string serialized form of the marshalled provenances.
     */
    public String serializeToString(List<ObjectMarshalledProvenance> marshalledProvenances);

    /**
     * Serializes the list of marshalled provenances to the specified file.
     * @param marshalledProvenances The provenances to serialize.
     * @param path The path to serialize to.
     * @throws IOException If the file could not be written.
     */
    public void serializeToFile(List<ObjectMarshalledProvenance> marshalledProvenances, Path path) throws IOException;

    /**
     * Marshalls and serializes the supplied provenance to a string.
     * @param provenance The provenance to serialize.
     * @return A string serialized form of the marshalled provenances.
     */
    default public String marshalAndSerialize(ObjectProvenance provenance) {
        return serializeToString(ProvenanceUtil.marshalProvenance(provenance));
    }

    /**
     * Marhsalls and serializes the supplied provenance to the specified file.
     * @param provenance The provenance to serialize.
     * @param path The path to serialize to.
     * @throws IOException If the file could not be written.
     */
    default public void marshalAndSerialize(ObjectProvenance provenance, Path path) throws IOException {
        serializeToFile(ProvenanceUtil.marshalProvenance(provenance),path);
    }
}
