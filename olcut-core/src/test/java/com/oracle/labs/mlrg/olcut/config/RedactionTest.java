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

package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.config.test.RedactedConfigurable;
import com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RedactionTest {
    private File f;

    @BeforeEach
    public void setUp() throws IOException {
        f = File.createTempFile("config", ".xml");
        f.deleteOnExit();
    }

    @Test
    public void redactLoadedOnSave() throws IOException {
        ConfigurationManager source = new ConfigurationManager("redactionConfig.xml");
        RedactedConfigurable a = (RedactedConfigurable) source.lookup("a");
        Assertions.assertEquals("present-value",a.present);
        Assertions.assertEquals("unredacted-value",a.redacted);
        Assertions.assertEquals("unredacted-mandatory-value",a.mandatoryRedacted);
        source.save(f);
        try {
            ConfigurationManager test = new ConfigurationManager(f.toString());
            RedactedConfigurable red = (RedactedConfigurable) test.lookup("a");
            Assertions.fail("Should have thrown property exception due to unset mandatory field");
        } catch (PropertyException e) {
            //Pass
        }

        String redactedVal = "<redacted>";
        ConfigurationManager secondTest = new ConfigurationManager(new String[]{"-c",f.toString(),"--@a.mandatoryRedacted",redactedVal});
        RedactedConfigurable restored = (RedactedConfigurable) secondTest.lookup("a");
        Assertions.assertEquals("present-value",restored.present);
        Assertions.assertNull(restored.redacted);
        Assertions.assertEquals(redactedVal,restored.mandatoryRedacted);
    }

    @Test
    public void redactImportedOnSave() throws IOException {
        RedactedConfigurable a = new RedactedConfigurable("present-value","unredacted-value","unredacted-mandatory-value");
        Assertions.assertEquals("present-value",a.present);
        Assertions.assertEquals("unredacted-value",a.redacted);
        Assertions.assertEquals("unredacted-mandatory-value",a.mandatoryRedacted);
        ConfigurationManager source = new ConfigurationManager();
        source.importConfigurable(a,"a");
        source.save(f);
        try {
            ConfigurationManager test = new ConfigurationManager(f.toString());
            RedactedConfigurable red = (RedactedConfigurable) test.lookup("a");
            Assertions.fail("Should have thrown property exception due to unset mandatory field");
        } catch (PropertyException e) {
            //Pass
        }

        String redactedVal = "<redacted>";
        ConfigurationManager secondTest = new ConfigurationManager(new String[]{"-c",f.toString(),"--@a.mandatoryRedacted",redactedVal});
        RedactedConfigurable restored = (RedactedConfigurable) secondTest.lookup("a");
        Assertions.assertEquals("present-value",restored.present);
        Assertions.assertNull(restored.redacted);
        Assertions.assertEquals(redactedVal,restored.mandatoryRedacted);
    }

    @Test
    public void redactOnProvenance() throws IOException {
        RedactedConfigurable a = new RedactedConfigurable("present-value","unredacted-value","unredacted-mandatory-value");
        ObjectProvenance prov = a.getProvenance();
        List<ConfigurationData> provConfig = ProvenanceUtil.extractConfiguration(prov);

        String configName = "redactedconfigurable-0";

        ConfigurationManager test = new ConfigurationManager();
        test.addConfiguration(provConfig);
        try {
            RedactedConfigurable red = (RedactedConfigurable) test.lookup(configName);
            Assertions.fail("Should have thrown property exception due to unset mandatory field");
        } catch (PropertyException e) {
            //Pass
        }

        ConfigurationManager cm = new ConfigurationManager();
        cm.addConfiguration(ProvenanceUtil.extractConfiguration(prov));
        String redactedVal = "<redacted>";
        cm.overrideConfigurableProperty(configName,"mandatoryRedacted",new SimpleProperty(redactedVal));
        RedactedConfigurable restored = cm.lookupAll(RedactedConfigurable.class).get(0);
        Assertions.assertEquals("present-value",restored.present);
        Assertions.assertNull(restored.redacted);
        Assertions.assertEquals(redactedVal,restored.mandatoryRedacted);
    }

}
