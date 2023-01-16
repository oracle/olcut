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

package com.oracle.labs.mlrg.olcut.config.edn.test;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.edn.EdnConfigFactory;
import com.oracle.labs.mlrg.olcut.test.config.FooConfigurable;
import com.oracle.labs.mlrg.olcut.test.config.FooUserConfigurable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test ConfigurationManager behavior for a Configurable with 1+ inner Configurable.
 */
public class NestedConfigurablesTest {
    private static final Logger log = Logger.getLogger(NestedConfigurablesTest.class.getName());

    @BeforeAll
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }

    @Test
    public void testLoadFromXML() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(this.getClass().getName()+"|nestedConfigurablesConfig.edn");
        FooUserConfigurable user = (FooUserConfigurable) cm.lookup("user");
        assertNotNull(user);
        FooConfigurable foo = user.getFoo();
        assertNotNull(foo);
        assertEquals("foo1", foo.name);
        assertEquals(1, foo.value);
    }

    @Test
    public void testSave() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager(this.getClass().getName()+"|nestedConfigurablesConfig.edn");
        FooUserConfigurable u1 = (FooUserConfigurable) cm1.lookup("user");
        File tmp = mkTmp();
        cm1.save(tmp);
        assertEquals(2, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(tmp.toString()));
        FooUserConfigurable u2 = (FooUserConfigurable) cm2.lookup("user");
        assertEquals(u1.getFoo(), u2.getFoo());
    }

    @Test
    public void testImportConfigurable() throws IOException {
        FooUserConfigurable u1 = new FooUserConfigurable(new FooConfigurable("foo1", 1));
        ConfigurationManager cm1 = new ConfigurationManager();
        cm1.importConfigurable(u1);
        assertEquals(2, cm1.getNumInstantiated());
        File tmp = mkTmp();
        cm1.save(tmp);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(tmp.toString()));
        FooUserConfigurable u2 = (FooUserConfigurable) cm2.lookup("user");
        assertEquals(u1.getFoo(), u2.getFoo());
    }

    private static File mkTmp() throws IOException {
        File tmp = File.createTempFile("tmpConfig", ".edn");
        log.fine("created tmp file @ " + tmp.getAbsolutePath());
        tmp.deleteOnExit();
        return tmp;
    }

}
