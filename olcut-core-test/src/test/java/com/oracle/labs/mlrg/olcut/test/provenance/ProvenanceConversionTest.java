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

import com.oracle.labs.mlrg.olcut.provenance.ListProvenance;
import com.oracle.labs.mlrg.olcut.provenance.MapProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil;
import com.oracle.labs.mlrg.olcut.test.config.AllFieldsConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.provenance.impl.SkeletalConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.IntProvenance;
import com.oracle.labs.mlrg.olcut.util.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
public class ProvenanceConversionTest {
    @BeforeAll
    public static void setup() {
        Logger logger = Logger.getLogger(PropertySheet.class.getName());
        logger.setLevel(Level.SEVERE);
        logger = Logger.getLogger(SkeletalConfiguredObjectProvenance.class.getName());
        logger.setLevel(Level.OFF);
    }

    @Test
    public void conversionTest() {
        ConfigurationManager cm1 = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/provenance/example-provenance-config.xml");
        ExampleProvenancableConfigurable e = (ExampleProvenancableConfigurable) cm1.lookup("example-config");
        assertNotNull(e, "Failed to load example config");

        List<ConfigurationData> configs = ProvenanceUtil.extractConfiguration(e.getProvenance());
        assertEquals(8,configs.size());

        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.addConfiguration(configs);

        ExampleProvenancableConfigurable newE = (ExampleProvenancableConfigurable) cm2.lookup("exampleprovenancableconfigurable-0");
        assertNotNull(newE, "Failed to load config from provenance");

        assertEquals(e,newE);
    }

    @Test
    public void largeConversionTest() {
        ConfigurationManager cm1 = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/config/allConfig.xml");
        AllFieldsConfigurable e = (AllFieldsConfigurable) cm1.lookup("all-config");
        assertNotNull(e, "Failed to load example config");

        List<ConfigurationData> configs = ProvenanceUtil.extractConfiguration(e.getProvenance());
        assertEquals(14,configs.size());

        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.addConfiguration(configs);

        AllFieldsConfigurable newE = (AllFieldsConfigurable) cm2.lookup("allfieldsconfigurable-0");
        assertNotNull(newE, "Failed to load config from provenance");

        assertEquals(e,newE);
    }

    @Test
    public void marshallingTest() {
        ConfigurationManager cm1 = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/provenance/example-provenance-config.xml");
        ExampleProvenancableConfigurable e = (ExampleProvenancableConfigurable) cm1.lookup("example-config");
        assertNotNull(e, "Failed to load example config");

        ObjectProvenance provenance = e.getProvenance();

        List<ObjectMarshalledProvenance> marshalledProvenances = ProvenanceUtil.marshalProvenance(provenance);
        assertEquals(8,marshalledProvenances.size());

        ObjectProvenance unmarshalledProvenance = ProvenanceUtil.unmarshalProvenance(marshalledProvenances);

        assertEquals(provenance,unmarshalledProvenance);
    }

    @Test
    public void largeMarshallingTest() {
        ConfigurationManager cm1 = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/config/allConfig.xml");
        AllFieldsConfigurable e = (AllFieldsConfigurable) cm1.lookup("all-config");
        assertNotNull(e, "Failed to load example config");

        ObjectProvenance provenance = e.getProvenance();

        List<ObjectMarshalledProvenance> marshalledProvenances = ProvenanceUtil.marshalProvenance(provenance);
        assertEquals(4,marshalledProvenances.size());

        ObjectProvenance unmarshalledProvenance = ProvenanceUtil.unmarshalProvenance(marshalledProvenances);

        assertEquals(provenance,unmarshalledProvenance);
    }

    @Test
    public void recursiveMarshallingTest() {
        Provenance prov = constructProvenance(new SplittableRandom(42),5,3,"prov");

        assertNotNull(prov);

        SimpleObjectProvenance objProv = new SimpleObjectProvenance((ListProvenance<?>)prov);

        List<ObjectMarshalledProvenance> marshalledProvenance = ProvenanceUtil.marshalProvenance(objProv);

        assertEquals(1,marshalledProvenance.size());

        ObjectProvenance unmarshalledProvenance = ProvenanceUtil.unmarshalProvenance(marshalledProvenance);

        assertEquals(objProv,unmarshalledProvenance);
    }

    public static Provenance constructProvenance(SplittableRandom rng, int depth, int width, String key) {
        if (depth == 0) {
            // base case, generate primitives
            return new IntProvenance(key,rng.nextInt(30));
        } else if (depth % 2 == 0) {
            // even - generate map
            Map<String,Provenance> mapProv = new HashMap<>();
            for (int i = 0; i < width; i++) {
                String newKey = "d="+depth+",w="+i;
                mapProv.put(newKey,constructProvenance(rng,depth-1,width,newKey));
            }
            return new MapProvenance<>(mapProv);
        } else {
            // odd - generate list
            List<Provenance> listProv = new ArrayList<>();
            for (int i = 0; i < width; i++) {
                listProv.add(constructProvenance(rng,depth-1,width,key));
            }
            return new ListProvenance<>(listProv);
        }
    }

    public static final class SimpleObjectProvenance implements ObjectProvenance {
        private final ListProvenance<? extends Provenance> prov;

        public SimpleObjectProvenance(ListProvenance<? extends Provenance> prov) {
            this.prov = prov;
        }

        public SimpleObjectProvenance(Map<String,Provenance> prov) {
            this.prov = (ListProvenance<?>)prov.get("prov");
        }

        @Override
        public String getClassName() {
            return ProvenanceConversionTest.class.getName();
        }

        @Override
        public Iterator<Pair<String, Provenance>> iterator() {
            return Collections.singletonList(new Pair<>("prov",(Provenance)prov)).iterator();
        }

        @Override
        public String toString() {
            return "SimpleObjectProvenance{" +
                    "prov=" + prov +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SimpleObjectProvenance)) return false;
            SimpleObjectProvenance pairs = (SimpleObjectProvenance) o;
            return prov.equals(pairs.prov);
        }

        @Override
        public int hashCode() {
            return Objects.hash(prov);
        }
    }
}
