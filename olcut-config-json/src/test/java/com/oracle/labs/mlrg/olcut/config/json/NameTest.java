package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.ArrayStringConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.NamedConfigurable;
import com.oracle.labs.mlrg.olcut.config.StringConfigurable;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *
 */
public class NameTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test
    public void configurableNameTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("componentListConfig.json");
        ArrayStringConfigurable lc = (ArrayStringConfigurable) cm.lookup("stringconfigurablearray");
        assertEquals("stringconfigurablearray",lc.getName());
        StringConfigurable[] l = lc.getArray();
        assertEquals(3, l.length);
        String firstOne = l[0].one;
        assertEquals("alpha",firstOne);
        String secondOne = l[1].one;
        assertEquals("one",secondOne);
        String thirdOne = l[2].one;
        assertEquals("un",thirdOne);
    }

    @Test
    public void componentNameTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("nameConfig.json");
        NamedConfigurable nc = (NamedConfigurable) cm.lookup("monkeys");
        assertEquals("monkeys",nc.getName());
    }

}
