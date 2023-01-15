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

import com.oracle.labs.mlrg.olcut.test.config.BasicConfigurable;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.test.config.ListTypeConfigurable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test for using the @Config annotation.
 */
public class TypeConfigTest {

    @BeforeAll
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new ProtoTxtConfigFactory());
    }

    @Test
    public void defaultValues() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("typeConfig.pbtxt");
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("default");
        assertEquals(bc1.s, "default");
        assertEquals(bc1.i, 16);
        assertEquals(bc1.bigI.intValue(), 17);
        assertEquals(bc1.l, 18);
        assertEquals(bc1.bigL.longValue(), 19);
        assertEquals(bc1.d, 21.0, 1E-9);
        assertEquals(bc1.bigD, 22.0, 1E-9);
    }

    @Test
    public void configuredTypes() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("typeConfig.pbtxt");
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("type-a");
        assertEquals(bc1.s, "one");
        assertEquals(bc1.i, 2);
        assertEquals(bc1.d, 3.0, 1E-9);
        bc1 = (BasicConfigurable) cm1.lookup("type-b");
        assertEquals(bc1.s, "two");
        assertEquals(bc1.i, 3);
        assertEquals(bc1.d, 6.3, 1E-9);
    }
    
    @Test
    public void listTypes() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("typeConfig.pbtxt");
        ListTypeConfigurable lc1 = (ListTypeConfigurable) cm1.lookup("type-list");
        Configurable[] cl = lc1.getList();
        BasicConfigurable bc1 = (BasicConfigurable) cl[0];
        assertEquals(bc1.s, "default");
        assertEquals(bc1.i, 16);
        assertEquals(bc1.bigI.intValue(), 17);
        assertEquals(bc1.l, 18);
        assertEquals(bc1.bigL.longValue(), 19);
        assertEquals(bc1.d, 21.0, 1E-9);
        assertEquals(bc1.bigD, 22.0, 1E-9);
        bc1 = (BasicConfigurable) cl[1];
        assertEquals(bc1.s, "one");
        assertEquals(bc1.i, 2);
        assertEquals(bc1.d, 3.0, 1E-9);
        bc1 = (BasicConfigurable) cl[2];
        assertEquals(bc1.s, "two");
        assertEquals(bc1.i, 3);
        assertEquals(bc1.d, 6.3, 1E-9);
    }

}
