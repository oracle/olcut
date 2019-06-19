/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.labs.mlrg.olcut.config;


import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 *
 */
public class OverrideTest {

    public OverrideTest() {
    }


    @Test
    public void overrideWithSameType() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("overrideConfig.xml");
        StringConfigurable sc = (StringConfigurable) cm.lookup("a");
        assertEquals("a", sc.one);
        assertEquals("b", sc.two);
        assertEquals("c", sc.three);
        sc = (StringConfigurable) cm.lookup("b");
        assertEquals("a", sc.one);
        assertEquals("b", sc.two);
        assertEquals("d", sc.three);
    }

    @Test
    public void doubleOverride() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("overrideConfig.xml");
        StringConfigurable sc = (StringConfigurable) cm.lookup("a");
        assertEquals("a", sc.one);
        assertEquals("b", sc.two);
        assertEquals("c", sc.three);
        sc = (StringConfigurable) cm.lookup("b");
        assertEquals("a", sc.one);
        assertEquals("b", sc.two);
        assertEquals("d", sc.three);
        sc = (StringConfigurable) cm.lookup("bsub");
        assertEquals("a", sc.one);
        assertEquals("b", sc.two);
        assertEquals("e", sc.three);
    }
    
    @Test
    public void overrideWithSubType() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("overrideConfig.xml");
        StringConfigurable sc = (StringConfigurable) cm.lookup("a");
        assertEquals("a", sc.one);
        assertEquals("b", sc.two);
        assertEquals("c", sc.three);
        StringleConfigurable sc2 = (StringleConfigurable) cm.lookup("c1");
        assertEquals("a", sc2.one);
        assertEquals("b", sc2.two);
        assertEquals("c", sc2.three);
        assertEquals("e", sc2.four);
        sc2 = (StringleConfigurable) cm.lookup("c2");
        assertEquals("a", sc2.one);
        assertEquals("b", sc2.two);
        assertEquals("d", sc2.three);
        assertEquals("e", sc2.four);
    }
}
