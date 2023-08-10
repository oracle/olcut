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

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.test.config.StringConfigurable;
import com.oracle.labs.mlrg.olcut.test.config.StringListConfigurable;
import com.oracle.labs.mlrg.olcut.util.Util;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.config.ConfigurationManager.createModuleResourceString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class GlobalPropertyTest {

    public GlobalPropertyTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void recursive() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
        StringConfigurable sc = (StringConfigurable) cm.lookup("recursive");
        Assertions.assertEquals("ab",sc.one);
        Assertions.assertEquals("abc",sc.two);
        Assertions.assertEquals("gamma",sc.three);
    }
    
    @Test
    public void noProperty() {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
            StringConfigurable sc = (StringConfigurable) cm.lookup("unknown");
        });
    }

    @Test
    public void badlyFormed() {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
            StringConfigurable sc = (StringConfigurable) cm.lookup("badlyformed");
        });
    }

    @Test
    public void invalidGlobalProperty() {
        assertThrows(ConfigLoaderException.class, () -> new ConfigurationManager(createModuleResourceString(this.getClass(), "invalidGlobalPropertyConfig.xml")));
    }

    @Test
    public void simpleReplacement() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
        StringConfigurable sc = (StringConfigurable) cm.lookup("simple");
        Assertions.assertEquals(sc.one, "alpha");
        Assertions.assertEquals(sc.two, "beta");
        Assertions.assertEquals(sc.three, "charlie");
    }

    @Test
    public void compoundReplacement() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
        StringConfigurable sc = (StringConfigurable) cm.lookup("compound");
        Assertions.assertEquals(sc.one, "alpha/beta");
        Assertions.assertEquals(sc.two, "betacharlie");
        Assertions.assertEquals(sc.three, "charlie:alpha");
    }
    
    @Test
    public void nonGlobals() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
        StringConfigurable sc = (StringConfigurable) cm.lookup("nonglobal");
        Assertions.assertEquals(sc.one, "${a");
        Assertions.assertEquals(sc.two, "$b}");
        Assertions.assertEquals(sc.three, "$c");
    }

    @Test
    public void recurse() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
        StringConfigurable sc = (StringConfigurable) cm.lookup("recurse");
        Assertions.assertEquals(sc.one, "alpha");
        Assertions.assertEquals(sc.two, "alpha");
        Assertions.assertEquals(sc.three, "alpha");
    }
    
    @Test
    public void recurse2() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
        StringConfigurable sc = (StringConfigurable) cm.lookup("recurse2");
        Assertions.assertEquals("alpha/bar", sc.one);
        Assertions.assertEquals(sc.two, "x");
        Assertions.assertEquals(sc.three, "y");
    }
    
    @Test
    public void recurse3() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
        StringConfigurable sc = (StringConfigurable) cm.lookup("recurse3");
        Assertions.assertEquals("/tmp/alpha", sc.one);
        Assertions.assertEquals(sc.two, "/tmp/alpha/bpath");
        Assertions.assertEquals(sc.three, "y");
        assertEquals("/tmp/alpha", cm.getGlobalProperty("apath"));
    }
    
    @Test
    public void compoundRecurse() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
        StringConfigurable sc = (StringConfigurable) cm.lookup("compoundrecurse");
        Assertions.assertEquals(sc.one, "one beta/alpha");
        Assertions.assertEquals(sc.two, "two charlie/alpha/beta/alpha");
        Assertions.assertEquals(sc.three, "three alpha/beta/charlie");
    }
    
    @Test
    public void distinguishedProps() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
        StringConfigurable sc = (StringConfigurable) cm.lookup("distinguished");
        Assertions.assertEquals(Util.getHostName(), sc.one);
        Assertions.assertEquals(System.getProperty("user.name"), sc.two);
    }
    
    @Test
    public void stringList() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "globalPropertyConfig.xml"));
        StringListConfigurable slc = (StringListConfigurable) cm.lookup("listTest");
        Assertions.assertEquals("alpha", slc.strings.get(0));
        Assertions.assertEquals("beta", slc.strings.get(1));
        Assertions.assertEquals("alpha/beta", slc.strings.get(2));
        Assertions.assertEquals("intro/beta", slc.strings.get(3));
        Assertions.assertEquals("alpha/extro", slc.strings.get(4));
    }
}
