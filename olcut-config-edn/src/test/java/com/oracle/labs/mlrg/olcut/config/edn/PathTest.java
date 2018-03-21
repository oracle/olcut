package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PathConfigurable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class PathTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @Test
    public void test() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("pathConfig.edn");
        PathConfigurable pc = (PathConfigurable) cm.lookup(
                "pathTest");
        String actualPath = pc.getPath().toString();
        actualPath = actualPath.replace('\\', '/');
        
        assertEquals("/this/is/a/test/path", actualPath);
    }

}
