package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ArrayStringConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.NamedConfigurable;
import com.oracle.labs.mlrg.olcut.config.StringConfigurable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class NameTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @Test
    public void configurableNameTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("componentListConfig.edn");
        ArrayStringConfigurable lc = (ArrayStringConfigurable) cm.lookup("stringconfigurablearray");
        assertEquals("stringconfigurablearray",lc.getName());
        StringConfigurable[] l = lc.getArray();
        assertTrue(l.length == 3);
        String firstOne = l[0].one;
        assertEquals("alpha",firstOne);
        String secondOne = l[1].one;
        assertEquals("one",secondOne);
        String thirdOne = l[2].one;
        assertEquals("un",thirdOne);
    }

    @Test
    public void componentNameTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("nameConfig.edn");
        NamedConfigurable nc = (NamedConfigurable) cm.lookup("monkeys");
        assertEquals("monkeys",nc.getName());
    }

}
