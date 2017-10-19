package com.oracle.labs.mlrg.olcut.config;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class NameTest {

    @Test
    public void configurableNameTest() throws IOException {
        URL cu = getClass().getResource("componentListConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
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
        URL cu = getClass().getResource("nameConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        NamedConfigurable nc = (NamedConfigurable) cm.lookup("monkeys");
        assertEquals("monkeys",nc.getName());
    }

}
