package com.sun.labs.util.props;

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
        URL cu = getClass().getResource("pathConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        PathConfigurable pc = (PathConfigurable) cm.lookup(
                "pathTest");
        assertEquals("/this/is/a/test/path", pc.getPath().toString());
    }

}
