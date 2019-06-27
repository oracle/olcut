package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.IntProvenance;
import com.oracle.labs.mlrg.olcut.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;

/**
 *
 */
public class ProvenanceConversionTest {
    @Test
    public void conversionTest() {
        ConfigurationManager cm1 = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/provenance/example-provenance-config.xml");
        ExampleProvenancableConfigurable e = (ExampleProvenancableConfigurable) cm1.lookup("example-config");
        Assert.assertNotNull("Failed to load example config", e);

        List<ConfigurationData> configs = ProvenanceUtil.extractConfiguration(e.getProvenance());
        Assert.assertEquals(8,configs.size());

        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.addConfiguration(configs);

        ExampleProvenancableConfigurable newE = (ExampleProvenancableConfigurable) cm2.lookup("exampleprovenancableconfigurable-0");
        Assert.assertNotNull("Failed to load config from provenance", newE);

        Assert.assertEquals(e,newE);
    }

    @Test
    public void marshallingTest() {
        ConfigurationManager cm1 = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/provenance/example-provenance-config.xml");
        ExampleProvenancableConfigurable e = (ExampleProvenancableConfigurable) cm1.lookup("example-config");
        Assert.assertNotNull("Failed to load example config", e);

        ObjectProvenance provenance = e.getProvenance();

        List<ObjectMarshalledProvenance> marshalledProvenances = ProvenanceUtil.marshalProvenance(provenance);
        Assert.assertEquals(8,marshalledProvenances.size());

        ObjectProvenance unmarshalledProvenance = ProvenanceUtil.unmarshalProvenance(marshalledProvenances);

        Assert.assertEquals(provenance,unmarshalledProvenance);
    }

    @Test
    public void recursiveMarshallingTest() {
        Provenance prov = constructProvenance(new SplittableRandom(42),5,3,"prov");

        Assert.assertNotNull(prov);

        SimpleObjectProvenance objProv = new SimpleObjectProvenance((ListProvenance)prov);

        List<ObjectMarshalledProvenance> marshalledProvenance = ProvenanceUtil.marshalProvenance(objProv);

        Assert.assertEquals(1,marshalledProvenance.size());

        ObjectProvenance unmarshalledProvenance = ProvenanceUtil.unmarshalProvenance(marshalledProvenance);

        Assert.assertEquals(objProv,unmarshalledProvenance);
    }

    private static Provenance constructProvenance(SplittableRandom rng, int depth, int width, String key) {
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

    private static final class SimpleObjectProvenance implements ObjectProvenance {
        private final ListProvenance<Provenance> prov;

        public SimpleObjectProvenance(ListProvenance<Provenance> prov) {
            this.prov = prov;
        }

        public SimpleObjectProvenance(Map<String,Provenance> prov) {
            this.prov = (ListProvenance)prov.get("prov");
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
