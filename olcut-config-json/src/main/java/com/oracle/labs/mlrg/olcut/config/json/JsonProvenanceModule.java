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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.oracle.labs.mlrg.olcut.provenance.io.MarshalledProvenance;

/**
 * The {@link MarshalledProvenance} serialization module.
 */
public class JsonProvenanceModule extends SimpleModule {
    static final String MARSHALLED_CLASS = "marshalled-class";
    static final String LIST = "list";
    static final String MAP = "map";
    static final String KEY = "key";
    static final String VALUE = "value";
    static final String PROVENANCE_CLASS = "provenance-class";
    static final String ADDITIONAL = "additional";
    static final String IS_REFERENCE = "is-reference";
    static final String OBJECT_NAME = "object-name";
    static final String OBJECT_CLASS_NAME = "object-class-name";

    private static final String NAME = "JsonProvenanceModule";

    public JsonProvenanceModule() {
        super(NAME, new Version(5, 0, 0, null, "com.oracle.labs.mlrg.olcut", "olcut-config-json"));
        addSerializer(MarshalledProvenance.class, new JsonProvenanceSerializer(MarshalledProvenance.class));
        addDeserializer(MarshalledProvenance.class, new JsonProvenanceDeserializer(MarshalledProvenance.class));
    }
}
