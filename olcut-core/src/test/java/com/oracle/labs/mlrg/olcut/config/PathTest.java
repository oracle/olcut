package com.oracle.labs.mlrg.olcut.config;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class PathTest {

    @Test
    public void test() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("pathConfig.xml");
        PathConfigurable pc = (PathConfigurable) cm.lookup(
                "pathTest");
        String actualPath = pc.getPath().toString();
        actualPath = actualPath.replace('\\', '/');
        
        assertEquals("/this/is/a/test/path", actualPath);
    }

}
