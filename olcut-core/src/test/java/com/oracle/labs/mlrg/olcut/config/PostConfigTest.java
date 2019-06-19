package com.oracle.labs.mlrg.olcut.config;

import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that the postConfig method is properly called and updates the fields before lookup returns.
 */
public class PostConfigTest {

    public PostConfigTest() { }

    @Test
    public void postConfigTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("postConfig.xml");
        PostConfigurable p = (PostConfigurable) cm.lookup("post");
        assertEquals("Monkeys",p.one);
        assertEquals("Gorillas",p.two);
    }
    
}
