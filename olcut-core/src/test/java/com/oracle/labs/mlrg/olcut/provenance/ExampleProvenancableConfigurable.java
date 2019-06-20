package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.provenance.ExampleProvenancableConfigurable.ExampleProvenance;
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
    public static class ExampleProvenance implements ConfiguredObjectProvenance {

        private final String className;

        private final DoubleProvenance doubleField;
        private final ListProvenance<IntProvenance> intArrayField;
        private final ListProvenance<ExampleProvenance> examples;
        private final MapProvenance<StringProvenance> map;

        public ExampleProvenance(ExampleProvenancableConfigurable host) {
            this.className = host.getClass().getName();
            this.doubleField = new DoubleProvenance("doubleField",host.doubleField);
            List<IntProvenance> ints = new ArrayList<>();
            for (int i = 0; i < host.intArrayField.length; i++) {
                ints.add(new IntProvenance("intArrayField", host.intArrayField[i]));
            }
            this.intArrayField = new ListProvenance<>(ints);
            this.examples = ListProvenance.createListProvenance(host.examples);
            Map<String,StringProvenance> strings = new HashMap<>();
            for (Map.Entry<String,String> e : host.map.entrySet()) {
                strings.put(e.getKey(),new StringProvenance(e.getKey(),e.getValue()));
            }
            this.map = new MapProvenance<>(strings);
        }

        @Override
        public Map<String, Provenance> getConfiguredParameters() {
            Map<String,Provenance> outputMap = new HashMap<>();

            outputMap.put("doubleField",doubleField);
            outputMap.put("intArrayField",intArrayField);
            outputMap.put("examples",examples);
            outputMap.put("map",map);

            return outputMap;
        }

        @Override
        public String getClassName() {
            return className;
        }
    }
}
