package com.oracle.labs.mlrg.olcut.provenance;

import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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
}
