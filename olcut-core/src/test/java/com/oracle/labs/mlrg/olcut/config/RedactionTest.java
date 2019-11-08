package com.oracle.labs.mlrg.olcut.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class RedactionTest {
    private File f;

    @BeforeEach
    public void setUp() throws IOException {
        f = File.createTempFile("config", ".xml");
        f.deleteOnExit();
    }

    @Test
    public void redactOnSave() throws IOException {
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

        String redactedVal = "mr";
        ConfigurationManager secondTest = new ConfigurationManager(new String[]{"-c",f.toString(),"--@a.mandatoryRedacted",redactedVal});
        RedactedConfigurable restored = (RedactedConfigurable) secondTest.lookup("a");
        Assertions.assertEquals("present-value",restored.present);
        Assertions.assertNull(restored.redacted);
        Assertions.assertEquals(redactedVal,restored.mandatoryRedacted);
    }

}
