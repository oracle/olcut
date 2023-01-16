/*
 * Copyright (c) 2004-2021, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config.json.test;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.json.JsonConfigFactory;
import com.oracle.labs.mlrg.olcut.config.json.JsonProvenanceSerialization;
import com.oracle.labs.mlrg.olcut.provenance.ListProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ProvenanceSerializationException;
import com.oracle.labs.mlrg.olcut.test.provenance.ExampleProvenancableConfigurable;
import com.oracle.labs.mlrg.olcut.test.provenance.SimpleObjectProvenance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.SplittableRandom;

import static com.oracle.labs.mlrg.olcut.test.provenance.ProvenanceTestUtils.constructProvenance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
public class JsonProvenanceSerializationTest {
    private final JsonProvenanceSerialization marshaller = new JsonProvenanceSerialization(true);

    @BeforeEach
    public void setUp() throws IOException {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test
    public void marshallingTest() throws ProvenanceSerializationException {
        ConfigurationManager cm1 = new ConfigurationManager(this.getClass().getName()+"|example-provenance-config.xml");
        ExampleProvenancableConfigurable e = (ExampleProvenancableConfigurable) cm1.lookup("example-config");
        assertNotNull(e, "Failed to load example config");

        ObjectProvenance provenance = e.getProvenance();

        List<ObjectMarshalledProvenance> marshalledProvenances = ProvenanceUtil.marshalProvenance(provenance);
        assertEquals(8,marshalledProvenances.size());

        String jsonResult = marshaller.serializeToString(marshalledProvenances);

        List<ObjectMarshalledProvenance> jsonProvenances = marshaller.deserializeFromString(jsonResult);

        ObjectProvenance unmarshalledProvenance = ProvenanceUtil.unmarshalProvenance(jsonProvenances);

        assertEquals(provenance,unmarshalledProvenance);
    }

    @Test
    public void recursiveMarshallingTest() throws ProvenanceSerializationException {
        Provenance prov = constructProvenance(new SplittableRandom(42),5,3,"prov");

        assertNotNull(prov);

        SimpleObjectProvenance objProv = new SimpleObjectProvenance((ListProvenance<?>)prov);

        List<ObjectMarshalledProvenance> marshalledProvenance = ProvenanceUtil.marshalProvenance(objProv);

        assertEquals(1,marshalledProvenance.size());

        String jsonResult = marshaller.serializeToString(marshalledProvenance);

        List<ObjectMarshalledProvenance> jsonProvenances = marshaller.deserializeFromString(jsonResult);

        ObjectProvenance unmarshalledProvenance = ProvenanceUtil.unmarshalProvenance(jsonProvenances);

        assertEquals(objProv,unmarshalledProvenance);
    }

}
