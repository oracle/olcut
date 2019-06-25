package com.oracle.labs.mlrg.olcut.provenance.io;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public final class ObjectMarshalledProvenance implements MarshalledProvenance {

    private final Map<String,FlatMarshalledProvenance> map;

    private final String objectClassName;

    private final String provenanceClassName;

    public ObjectMarshalledProvenance(Map<String, FlatMarshalledProvenance> map, String objectClassName, String provenanceClassName) {
        this.map = Collections.unmodifiableMap(map);
        this.objectClassName = objectClassName;
        this.provenanceClassName = provenanceClassName;
    }

    public Map<String, FlatMarshalledProvenance> getMap() {
        return map;
    }

    public String getObjectClassName() {
        return objectClassName;
    }

    public String getProvenanceClassName() {
        return provenanceClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectMarshalledProvenance)) return false;
        ObjectMarshalledProvenance that = (ObjectMarshalledProvenance) o;
        return map.equals(that.map) &&
                objectClassName.equals(that.objectClassName) &&
                provenanceClassName.equals(that.provenanceClassName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, objectClassName, provenanceClassName);
    }
}
