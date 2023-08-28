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

package com.oracle.labs.mlrg.olcut.test.provenance;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.provenance.ConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ListProvenance;
import com.oracle.labs.mlrg.olcut.provenance.MapProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenancable;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceException;
import com.oracle.labs.mlrg.olcut.test.provenance.ExampleProvenancableConfigurable.ExampleProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DoubleProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.IntProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.StringProvenance;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Test class for the provenance system.
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

    @Override
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
        public ExampleProvenance(Map<String, Provenance> provenances) {
            this.className = ObjectProvenance.checkAndExtractProvenance(provenances,ObjectProvenance.CLASS_NAME,StringProvenance.class,ExampleProvenance.class.getName()).getValue();
            this.map = ObjectProvenance.checkAndExtractProvenance(provenances,MAP,MapProvenance.class,ExampleProvenance.class.getName());
            Optional<DoubleProvenance> opt = ObjectProvenance.maybeExtractProvenance(provenances,DOUBLE_FIELD,DoubleProvenance.class,ExampleProvenance.class.getName());
            if (opt.isPresent()) {
                this.doubleField = opt.get();
            } else {
                throw new ProvenanceException("Failed to find " + DOUBLE_FIELD + " when constructing ExampleProvenance");
            }
            Optional<DoubleProvenance> notPresentOpt = ObjectProvenance.maybeExtractProvenance(provenances,"DEFINITELY-NOT-HERE",DoubleProvenance.class,ExampleProvenance.class.getName());
            if (notPresentOpt.isPresent()) {
                Assertions.fail("Found a provenance which wasn't there");
            }

            try {
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
