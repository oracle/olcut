package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
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
