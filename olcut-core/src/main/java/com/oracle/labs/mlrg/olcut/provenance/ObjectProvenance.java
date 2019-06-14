package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil.HashType;
import com.oracle.labs.mlrg.olcut.util.Pair;

/**
 *
 */
public interface ObjectProvenance extends Provenance, Iterable<Pair<String,Provenance>> {

    public static final String CLASS_NAME = "class_name";
    public static final HashType DEFAULT_HASH_TYPE = HashType.SHA256;

    /**
     * Returns the class name of the object which produced this ObjectProvenance instance.
     * @return The class name.
     */
    public String getClassName();

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

}
