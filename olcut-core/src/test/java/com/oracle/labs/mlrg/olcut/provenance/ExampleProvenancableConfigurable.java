package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.provenance.ExampleProvenancableConfigurable.ExampleProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DoubleProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.IntProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.StringProvenance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public final class ExampleProvenancableConfigurable implements Configurable, Provenancable<ExampleProvenance> {

    @Config
    public double doubleField = Double.NaN;

    @Config
    public int[] intArrayField = new int[0];

    @Config
    public List<ExampleProvenancableConfigurable> examples = new ArrayList<>();

    @Config
    public Map<String,String> map = new HashMap<>();

    private ExampleProvenance provenance;

    public void postConfig() {
        this.provenance = new ExampleProvenance(this);
    }

    @Override
    public ExampleProvenance getProvenance() {
        return provenance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExampleProvenancableConfigurable)) return false;
        ExampleProvenancableConfigurable that = (ExampleProvenancableConfigurable) o;
        return Double.compare(that.doubleField, doubleField) == 0 &&
                Arrays.equals(intArrayField, that.intArrayField) &&
                examples.equals(that.examples) &&
                map.equals(that.map);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(doubleField, examples, map);
        result = 31 * result + Arrays.hashCode(intArrayField);
        return result;
    }

    /**
     *
     */
    public static final class ExampleProvenance implements ConfiguredObjectProvenance {
        private static final String DOUBLE_FIELD = "doubleField";
        private static final String INT_ARRAY_FIELD = "intArrayField";
        private static final String EXAMPLES = "examples";
        private static final String MAP = "map";

        private final String className;

        private final DoubleProvenance doubleField;
        private final ListProvenance<IntProvenance> intArrayField;
        private final ListProvenance<ExampleProvenance> examples;
        private final MapProvenance<StringProvenance> map;

        public ExampleProvenance(ExampleProvenancableConfigurable host) {
            this.className = host.getClass().getName();
            this.doubleField = new DoubleProvenance(DOUBLE_FIELD,host.doubleField);
            List<IntProvenance> ints = new ArrayList<>();
            for (int i = 0; i < host.intArrayField.length; i++) {
                ints.add(new IntProvenance(INT_ARRAY_FIELD, host.intArrayField[i]));
            }
            this.intArrayField = new ListProvenance<>(ints);
            this.examples = ListProvenance.createListProvenance(host.examples);
            Map<String,StringProvenance> strings = new HashMap<>();
            for (Map.Entry<String,String> e : host.map.entrySet()) {
                strings.put(e.getKey(),new StringProvenance(e.getKey(),e.getValue()));
            }
            this.map = new MapProvenance<>(strings);
        }

        @SuppressWarnings("unchecked")
        public ExampleProvenance(Map<String,Provenance> provenances) {
            try {
                if (provenances.containsKey(ObjectProvenance.CLASS_NAME)) {
                    this.className = provenances.get(ObjectProvenance.CLASS_NAME).toString();
                } else {
                    throw new ProvenanceException("Failed to find class name when constructing ExampleProvenance");
                }
                if (provenances.containsKey(DOUBLE_FIELD)) {
                    this.doubleField = (DoubleProvenance) provenances.get(DOUBLE_FIELD);
                } else {
                    throw new ProvenanceException("Failed to find " + DOUBLE_FIELD + " when constructing ExampleProvenance");
                }
                if (provenances.containsKey(INT_ARRAY_FIELD)) {
                    this.intArrayField = (ListProvenance<IntProvenance>) provenances.get(INT_ARRAY_FIELD);
                } else {
                    throw new ProvenanceException("Failed to find " + INT_ARRAY_FIELD + " when constructing ExampleProvenance");
                }
                if (provenances.containsKey(EXAMPLES)) {
                    this.examples = (ListProvenance<ExampleProvenance>) provenances.get(EXAMPLES);
                } else {
                    throw new ProvenanceException("Failed to find " + EXAMPLES + " when constructing ExampleProvenance");
                }
                if (provenances.containsKey(MAP)) {
                    this.map = (MapProvenance<StringProvenance>) provenances.get(MAP);
                } else {
                    throw new ProvenanceException("Failed to find " + MAP + " when constructing ExampleProvenance");
                }
            } catch (ClassCastException e) {
                throw new ProvenanceException("Incorrect type found in provenance, did not match the field type.",e);
            }
        }

        @Override
        public Map<String, Provenance> getConfiguredParameters() {
            Map<String,Provenance> outputMap = new HashMap<>();

            outputMap.put(DOUBLE_FIELD,doubleField);
            outputMap.put(INT_ARRAY_FIELD,intArrayField);
            outputMap.put(EXAMPLES,examples);
            outputMap.put(MAP,map);

            return outputMap;
        }

        @Override
        public String getClassName() {
            return className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExampleProvenance)) return false;
            ExampleProvenance pairs = (ExampleProvenance) o;
            return className.equals(pairs.className) &&
                    doubleField.equals(pairs.doubleField) &&
                    intArrayField.equals(pairs.intArrayField) &&
                    examples.equals(pairs.examples) &&
                    map.equals(pairs.map);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, doubleField, intArrayField, examples, map);
        }

        @Override
        public String toString() {
            return "ExampleProvenance{" +
                    "className='" + className + '\'' +
                    ", doubleField=" + doubleField +
                    ", intArrayField=" + intArrayField +
                    ", examples=" + examples +
                    ", map=" + map +
                    '}';
        }
    }
}
