/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config.protobuf;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.StringConfigurable;
import com.oracle.labs.mlrg.olcut.config.StringleConfigurable;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 *
 */
public class OverrideTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new ProtoConfigFactory());
        ConfigurationManager.addFileFormatFactory(new ProtoTxtConfigFactory());
    }

    @Test
    public void overrideWithSameType() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("overrideConfig.pb");
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
        ConfigurationManager cm = new ConfigurationManager("overrideConfig.pb");
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
        ConfigurationManager cm = new ConfigurationManager("overrideConfig.pb");
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

    @Test
    public void overrideIncorrectName() {
        Assertions.assertThrows(ConfigLoaderException.class,
                () -> {
                    ConfigurationManager cm = new ConfigurationManager("overrideIncorrect.pbtxt");
                });
    }
}
