package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PostConfigurable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests that the postConfig method is properly called and updates the fields before lookup returns.
 */
public class PostConfigTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    public PostConfigTest() { }

    @Test
    public void postConfigTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("postConfig.edn");
        PostConfigurable p = (PostConfigurable) cm.lookup("post");
        Assert.assertEquals("Monkeys",p.one);
        Assert.assertEquals("Gorillas",p.two);
    }
    
}
