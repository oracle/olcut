package com.oracle.labs.mlrg.olcut.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringMultiwordTest {

    @Test
    public void multiWord() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringConfig.xml");
        StringConfigurable sc = (StringConfigurable) cm.lookup(
                "words");
        assertEquals("rhesus monkey", sc.one);
        assertEquals("-Xmx16g -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", sc.two);
        assertEquals("test/value whitespace", sc.three);

    }
}
