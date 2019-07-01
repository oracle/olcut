package com.oracle.labs.mlrg.olcut.config.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.provenance.ExampleProvenancableConfigurable;
import com.oracle.labs.mlrg.olcut.provenance.ListProvenance;
import com.oracle.labs.mlrg.olcut.provenance.MapProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceConversionTest.SimpleObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil;
import com.oracle.labs.mlrg.olcut.provenance.io.MarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.IntProvenance;
import com.oracle.labs.mlrg.olcut.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;

import static com.oracle.labs.mlrg.olcut.provenance.ProvenanceConversionTest.constructProvenance;

/**
 *
 */
public class JsonProvenanceConversionTest {
    private File f;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws IOException {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
        mapper = new ObjectMapper();
        mapper.registerModule(new JsonProvenanceModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        f = File.createTempFile("provenance", ".json");
        f.deleteOnExit();
    }

    @Test
    public void marshallingTest() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("/com/oracle/labs/mlrg/olcut/provenance/example-provenance-config.xml");
        ExampleProvenancableConfigurable e = (ExampleProvenancableConfigurable) cm1.lookup("example-config");
        Assert.assertNotNull("Failed to load example config", e);

        ObjectProvenance provenance = e.getProvenance();

        List<ObjectMarshalledProvenance> marshalledProvenances = ProvenanceUtil.marshalProvenance(provenance);
        Assert.assertEquals(8,marshalledProvenances.size());

        String jsonResult = mapper.writeValueAsString(marshalledProvenances);

        List<ObjectMarshalledProvenance> jsonProvenances = mapper.readValue(jsonResult, new TypeReference<List<MarshalledProvenance>>(){});

        ObjectProvenance unmarshalledProvenance = ProvenanceUtil.unmarshalProvenance(jsonProvenances);

        Assert.assertEquals(provenance,unmarshalledProvenance);
    }

    @Test
    public void recursiveMarshallingTest() throws IOException {
        Provenance prov = constructProvenance(new SplittableRandom(42),5,3,"prov");

        Assert.assertNotNull(prov);

        SimpleObjectProvenance objProv = new SimpleObjectProvenance((ListProvenance)prov);

        List<ObjectMarshalledProvenance> marshalledProvenance = ProvenanceUtil.marshalProvenance(objProv);

        Assert.assertEquals(1,marshalledProvenance.size());

        String jsonResult = mapper.writeValueAsString(marshalledProvenance);

        List<ObjectMarshalledProvenance> jsonProvenances = mapper.readValue(jsonResult, new TypeReference<List<MarshalledProvenance>>(){});

        ObjectProvenance unmarshalledProvenance = ProvenanceUtil.unmarshalProvenance(jsonProvenances);

        Assert.assertEquals(objProv,unmarshalledProvenance);
    }

}
