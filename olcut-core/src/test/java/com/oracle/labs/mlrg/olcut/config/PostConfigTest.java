package com.oracle.labs.mlrg.olcut.config;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that the postConfig method is properly called and updates the fields before lookup returns.
 */
public class PostConfigTest {

    public PostConfigTest() { }

    @Test
    public void postConfigTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("postConfig.xml");
        PostConfigurable p = (PostConfigurable) cm.lookup("post");
        Assert.assertEquals("Monkeys",p.one);
        Assert.assertEquals("Gorillas",p.two);
    }
    
}
