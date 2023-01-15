/*
 * Copyright (c) 2020, 2023, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.provenance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.provenance.impl.NullConfiguredProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.oracle.labs.mlrg.olcut.test.config.AllFieldsConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.util.IOUtil;

public class ProvenanceUtilTest {

    @BeforeAll
    public static void setup() {
        Logger logger = Logger.getLogger(PropertySheet.class.getName());
        logger.setLevel(Level.SEVERE);
    }

    @Test
    public void testNullFields() {
        TestProvenancableConfigurable test = new TestProvenancableConfigurable(25, Arrays.asList(5,4,3,2,1));

        ConfiguredObjectProvenance prov = test.getProvenance();

        assertTrue(prov.getConfiguredParameters().get("nullField") instanceof NullConfiguredProvenance);

        List<ConfigurationData> configList = ProvenanceUtil.extractConfiguration(prov);

        // The NullConfiguredProvenance should be an empty field in the configuration object that holds it so there is
        // only a single object provenance to convert into configuration data.
        assertEquals(1,configList.size());

        List<ObjectMarshalledProvenance> marshalledProvenances = ProvenanceUtil.marshalProvenance(prov);
        assertEquals(2,marshalledProvenances.size());

        ObjectProvenance unmarshalledProvenance = ProvenanceUtil.unmarshalProvenance(marshalledProvenances);

        assertEquals(prov,unmarshalledProvenance);
    }

    @Test
    public void testSerialize() throws Exception {
        File tempFile = File.createTempFile("serialized-provenancable", ".ser", new File("target"));
        tempFile.deleteOnExit();

        ConfigurationManager cm = new ConfigurationManager("allConfig.xml");
        AllFieldsConfigurable afc = (AllFieldsConfigurable) cm.lookup("all-config");
        cm.close();
        MyProvenancableClass mpc = new MyProvenancableClass(afc);
        IOUtil.serialize(mpc, tempFile.getPath());
        mpc = IOUtil.deserialize(tempFile.getPath(), MyProvenancableClass.class).get();
        assertEquals(afc, mpc.afc);
    }

    public static class MyProvenancableClass implements Serializable {
        private static final long serialVersionUID = 1L;
        public AllFieldsConfigurable afc;

        public MyProvenancableClass(AllFieldsConfigurable afc) {
            super();
            this.afc = afc;
        }

        private void readObject(ObjectInputStream inputStream) throws ClassNotFoundException, IOException {
            this.afc = (AllFieldsConfigurable) ProvenanceUtil.readObject(inputStream);
        }

        private void writeObject(ObjectOutputStream outputStream) throws IOException {
            ProvenanceUtil.writeObject(this.afc, outputStream);
        }
    }
}
