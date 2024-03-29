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

import java.io.File;
import java.io.IOException;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.test.config.FooMapConfigurable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.config.ConfigurationManager.createModuleResourceString;
import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class ConfigurablePropertyMapTest {

    public File f;

    @BeforeEach
    public void setUp() throws IOException {
        f = File.createTempFile("config", ".xml");
        f.deleteOnExit();
    }

    @Test
    public void configurablePropMap() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "configurablePropMap.xml"));

        FooMapConfigurable fm = (FooMapConfigurable) cm.lookup("fooMap");

        Assertions.assertEquals("foo1", fm.map.get("first").name);
        Assertions.assertEquals(1, fm.map.get("first").value);

        Assertions.assertEquals("foo2",fm.map.get("second").name);
        Assertions.assertEquals(2,fm.map.get("second").value);
    }

    @Test
    public void overriddenPropMap() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "configurablePropMap.xml"));

        FooMapConfigurable fm = (FooMapConfigurable) cm.lookup("overriddenMap");

        Assertions.assertEquals("foo3", fm.map.get("first").name);
        Assertions.assertEquals(20, fm.map.get("first").value);

        Assertions.assertEquals("foo2", fm.map.get("second").name);
        Assertions.assertEquals(2, fm.map.get("second").value);
    }

    @Test
    public void saveAllWithInstantiationGeneric() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager(createModuleResourceString(this.getClass(), "configurablePropMap.xml"));
        FooMapConfigurable s1 = (FooMapConfigurable) cm1.lookup("fooMap");
        cm1.save(f, true);
        assertEquals(3, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        FooMapConfigurable s2 = (FooMapConfigurable) cm2.lookup("fooMap");
        Assertions.assertEquals(s1, s2);
    }

    @Test
    public void saveAllWithNoInstantiationGeneric() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager(createModuleResourceString(this.getClass(), "configurablePropMap.xml"));
        cm1.save(f, true);
        assertEquals(0, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        FooMapConfigurable s1 = (FooMapConfigurable) cm1.lookup("fooMap");
        FooMapConfigurable s2 = (FooMapConfigurable) cm2.lookup("fooMap");
        Assertions.assertEquals(s1, s2);
    }
}
