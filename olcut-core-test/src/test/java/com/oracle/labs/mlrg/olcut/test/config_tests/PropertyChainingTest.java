/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.oracle.labs.mlrg.olcut.test.config_tests;


import java.io.IOException;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.test.config.StringConfigurable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.config.ConfigurationManager.createModuleResourceString;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the chain loading of XML files in a configuration.
 */
public class PropertyChainingTest {

    public PropertyChainingTest() { }


    @Test
    public void chainLoading() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "propertyChainingConfigA.xml"));
        String stringA = cm.getGlobalProperty("stringA");
        String stringB = cm.getGlobalProperty("stringB");
        String stringC = cm.getGlobalProperty("stringC");
        StringConfigurable sca = (StringConfigurable) cm.lookup("configA");
        StringConfigurable scb = (StringConfigurable) cm.lookup("configB");
        StringConfigurable scc = (StringConfigurable) cm.lookup("configC");
        assertEquals(stringA,"HK-47");
        assertEquals(stringB,"BB-8");
        assertEquals(stringC,"C3P0");
        Assertions.assertEquals(sca.one,"fileA");
        Assertions.assertEquals(scb.one,"fileB");
        Assertions.assertEquals(scc.one,"fileC");
    }

    @Test
    public void overlay() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "propertyChainingConfigA.xml"));
        String globalA = cm.getGlobalProperty("a");
        String globalB = cm.getGlobalProperty("b");
        String globalC = cm.getGlobalProperty("c");
        StringConfigurable sca = (StringConfigurable) cm.lookup("configA");
        StringConfigurable scb = (StringConfigurable) cm.lookup("configB");
        StringConfigurable scc = (StringConfigurable) cm.lookup("configC");
        assertEquals("angry",globalA);
        assertEquals(globalB,"bus");
        assertEquals(globalC,"closing");
        Assertions.assertEquals(sca.two,"bus");
        Assertions.assertEquals(scb.two,"bus");
        Assertions.assertEquals(scc.two,"bus");
        Assertions.assertEquals(sca.three,"closing");
        Assertions.assertEquals(scb.three,"closing");
        Assertions.assertEquals(scc.three,"closing");
    }
    
}
