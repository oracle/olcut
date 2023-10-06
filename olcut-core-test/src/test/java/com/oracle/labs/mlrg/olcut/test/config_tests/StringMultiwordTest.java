package com.oracle.labs.mlrg.olcut.test.config_tests;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.test.config.StringConfigurable;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.oracle.labs.mlrg.olcut.config.ConfigurationManager.createModuleResourceString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringMultiwordTest {

    @Test
    public void multiWord() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "stringConfig.xml"));
        StringConfigurable sc = (StringConfigurable) cm.lookup(
                "words");
        assertEquals("rhesus monkey", sc.one);
        assertEquals("-Xmx16g -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", sc.two);
        assertEquals("test/value whitespace", sc.three);

    }
}
