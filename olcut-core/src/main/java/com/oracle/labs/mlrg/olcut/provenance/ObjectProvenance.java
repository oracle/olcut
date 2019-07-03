package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil.HashType;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.util.Map;

/**
 * A provenance object which records object fields.
 *
 * Must record the class name of the host object so it can be recovered.
 *
 * All classes which implement this interface must expose a public constructor
 * which accepts a Map&lt;String,Provenance&gt; which is used in deserialisation,
 * and have consistent implementations of equals and hashCode.
 *
 * By convention all provenances which do not refer to an object field
 * use hyphens as separators for their name. Provenances which refer to an object field
 * use standard Java camel case.
 */
public interface ObjectProvenance extends Provenance, Iterable<Pair<String,Provenance>> {

    public static final String CLASS_NAME = "class-name";
    public static final HashType DEFAULT_HASH_TYPE = HashType.SHA256;

    /**
     * Returns the class name of the object which produced this ObjectProvenance instance.
     * @return The class name.
     */
    public String getClassName();

    /**
     * Generates a String representation of this provenance.
     *
     * Commonly used to implement toString.
     * @param name The name to give the provenance.
     * @return A string representation.
     */
    default public String generateString(String name) {
        StringBuilder sb = new StringBuilder();

        sb.append(name);
        sb.append("(");
        for (Pair<String,Provenance> p : this) {
            sb.append(p.getA());
            sb.append('=');
            sb.append(p.getB().toString());
            sb.append(',');
        }
        sb.replace(sb.length()-1,sb.length(),")");

        return sb.toString();
    }

    /**
     * Removes the specified Provenance from the supplied map and returns it. Checks that it's the right type,
     * and casts to it before returning.
     *
     * Throws ProvenanceException if it's not found or it's an incorrect type.
     * @param map The map to check.
     * @param key The key to look up.
     * @param type The type to check the value against.
     * @param provClassName The name of the requesting class (to ensure the exception has the appropriate error message).
     * @param <T> The type of the value.
     * @return The specified provenance object.
     * @throws ProvenanceException if the key is not found, or the value is not the requested type.
     */
    @SuppressWarnings("unchecked") // Guarded by isInstance check
    public static <T extends Provenance> T checkAndExtractProvenance(Map<String,Provenance> map, String key, Class<T> type, String provClassName) throws ProvenanceException {
        Provenance tmp = map.remove(key);
        if (tmp != null) {
            if (type.isInstance(tmp)) {
                return (T) tmp;
            } else {
                throw new ProvenanceException("Failed to cast " + key + " when constructing " + provClassName + ", found " + tmp);
            }
        } else {
            throw new ProvenanceException("Failed to find " + key + " when constructing " + provClassName);
        }
    }
}
