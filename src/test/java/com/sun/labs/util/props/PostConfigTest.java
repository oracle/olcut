package com.sun.labs.util.props;

import java.io.IOException;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that the postConfig method is properly called and updates the fields before lookup returns.
 */
public class PostConfigTest {

    public PostConfigTest() { }

    @Test
    public void postConfigTest() throws IOException {
        URL cu = getClass().getResource("postConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        PostConfigurable p = (PostConfigurable) cm.lookup("post");
        Assert.assertEquals("Monkeys",p.one);
        Assert.assertEquals("Gorillas",p.two);
    }
    
}
