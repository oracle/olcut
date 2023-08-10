/*
 * Copyright (c) 2004, 2023, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config.edn.test;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.edn.EdnConfigFactory;
import com.oracle.labs.mlrg.olcut.test.config.EnumConfigurable;
import com.oracle.labs.mlrg.olcut.config.PropertyException;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *
 */
public class EnumConfigurableTest {
    private static final String path = ConfigurationManager.createModuleResourceString(EnumConfigurableTest.class, "enumConfig.edn");

    @BeforeAll
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @Test
    public void both() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(path);
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("both");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.B, ec.enum2);
    }

    @Test public void set1() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(path);
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("set1");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.B, ec.enum2);
        assertTrue(ec.enumSet1.contains(EnumConfigurable.Type.A), "Missing A");
        assertTrue(ec.enumSet1.contains(EnumConfigurable.Type.B), "Missing B");
        assertEquals(2, ec.enumSet1.size(), "Too big: " + ec.enumSet1);
    }

    @Test public void defaultSet() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(path);
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("both");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.B, ec.enum2);
        assertTrue(ec.enumSet1.contains(EnumConfigurable.Type.A), "Missing A");
        assertTrue(ec.enumSet1.contains(EnumConfigurable.Type.F), "Missing F");
        assertEquals(2, ec.enumSet1.size(), "Too big: " + ec.enumSet1);
    }

    @Test
    public void badSetValue() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager(path);
            EnumConfigurable ec = (EnumConfigurable) cm.lookup("badset");
        });
    }

    @Test
    public void defaultValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(path);
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("default");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.A, ec.enum2);
    }

    @Test
    public void badValue() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager(path);
            EnumConfigurable ec = (EnumConfigurable) cm.lookup("badvalue");
        });
    }

    @Test
    public void globalValue() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(path);
        EnumConfigurable ec = (EnumConfigurable) cm.lookup("global");
        assertEquals(EnumConfigurable.Type.A, ec.enum1);
        assertEquals(EnumConfigurable.Type.A, ec.enum2);
    }
    
}