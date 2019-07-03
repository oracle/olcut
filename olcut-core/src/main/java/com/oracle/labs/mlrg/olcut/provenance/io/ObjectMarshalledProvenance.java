package com.oracle.labs.mlrg.olcut.provenance.io;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * A marshalled provenance representing an
 * {@link com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance} subclass.
 *
 * Contains the name of the object, the class name of the provenance's host object,
 * and the class name of the provenance object.
 */
public final class ObjectMarshalledProvenance implements MarshalledProvenance {

    private final Map<String,FlatMarshalledProvenance> map;

    private final String objectName;

    private final String objectClassName;

    private final String provenanceClassName;

    public ObjectMarshalledProvenance(String objectName, Map<String, FlatMarshalledProvenance> map, String objectClassName, String provenanceClassName) {
        this.objectName = objectName;
        this.map = Collections.unmodifiableMap(map);
        this.objectClassName = objectClassName;
        this.provenanceClassName = provenanceClassName;
    }

    public String getName() {
        return objectName;
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
