package com.oracle.labs.mlrg.olcut.config.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.oracle.labs.mlrg.olcut.provenance.io.MarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;

/**
 *
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
