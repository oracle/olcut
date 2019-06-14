package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.provenance.impl.NullConfiguredProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.StringProvenance;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Provenance for a specific object known to the config system.
 */
public interface ConfiguredObjectProvenance extends ObjectProvenance {

    /**
     * Returns a (possibly nested) map describing the configuration parameters of this object.
     * @return The configuration parameters.
     */
    public Map<String,Provenance> getConfiguredParameters();

    /**
     * Returns a map containing the derived values describing the specific instance of
     * the object. For example the number of times it's RNG was used, the number of features
     * in a specific dataset etc.
     *
     * Defaults to returning {@link Collections#emptyMap}.
     * @return The instance values.
     */
    default public Map<String, PrimitiveProvenance> getInstanceValues() {
        return Collections.emptyMap();
    }

    @Override
    default public Iterator<Pair<String, Provenance>> iterator() {
        ArrayList<Pair<String,Provenance>> iterable = new ArrayList<>();
        iterable.add(new Pair<>(CLASS_NAME,new StringProvenance(CLASS_NAME,getClassName())));
        for (Map.Entry<String,Provenance> m : getConfiguredParameters().entrySet()) {
            iterable.add(new Pair<>(m.getKey(),m.getValue()));
        }
        for (Map.Entry<String,PrimitiveProvenance> m : getInstanceValues().entrySet()) {
            iterable.add(new Pair<>(m.getKey(),m.getValue()));
        }
        return Collections.unmodifiableList(iterable).iterator();
    }

    public static ConfiguredObjectProvenance getEmptyProvenance(String className) {
        return new NullConfiguredProvenance(className);
    }
}
