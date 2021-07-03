/*
 * Copyright (c) 2004-2021, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


/**
 *
 */
public class ImportConfigTest {

    public ImportConfigTest() {
    }

    File f;


    @BeforeEach
    public void setUp() throws IOException {
        f = File.createTempFile("config", ".xml");
        f.deleteOnExit();
    }

    @AfterEach
    public void tearDown() {
        f.delete();
    }

    @Test
    public void importSimple() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("importConfig.xml");
        StringConfigurable sc1 = (StringConfigurable) cm1.lookup("b");
        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.importConfigurable(sc1, "a");
        cm2.save(f);
        cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        StringConfigurable sc2 = (StringConfigurable) cm2.lookup("a");
        assertEquals(sc1.one, sc2.one);
        assertEquals(sc1.two, sc2.two);
        assertEquals(sc1.three, sc2.three);
    }

    @Test
    public void importEnum() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("enumConfig.xml");
        EnumConfigurable ec1 = (EnumConfigurable) cm1.lookup("both");
        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.importConfigurable(ec1, "both");
        cm2.save(f);
        cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        EnumConfigurable ec2 = (EnumConfigurable) cm1.lookup("both");
        assertEquals(ec1, ec2);
    }

    @Test
    public void importEnumNonDefault() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("enumConfig.xml");
        EnumConfigurable ec1 = (EnumConfigurable) cm1.lookup("set1");
        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.importConfigurable(ec1, "set1");
        cm2.save(f);
        cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        EnumConfigurable ec2 = (EnumConfigurable) cm1.lookup("set1");
        assertEquals(ec1, ec2);
    }

    @Test
    public void importCombo() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("importConfig.xml");
        ComboConfigurable cc1 = (ComboConfigurable) cm1.lookup("a");
        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.importConfigurable(cc1, "a");
        cm2.save(f);
        cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        ComboConfigurable cc2 = (ComboConfigurable) cm2.lookup("a");
        assertEquals(cc1.alpha, cc2.alpha);
        assertEquals(cc1.sc.one, cc2.sc.one);
        assertEquals(cc1.sc.two, cc2.sc.two);
        assertEquals(cc1.sc.three, cc2.sc.three);
    }

    @Test
    public void importMultiCombo() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("importConfig.xml");
        L1Configurable l1 = (L1Configurable) cm1.lookup("l1");
        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.importConfigurable(l1, "l1");
        cm2.save(f);
        cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        L1Configurable l1n = (L1Configurable) cm2.lookup("l1");
        assertEquals(l1.s, l1n.s);
        assertEquals(l1.c.s, l1n.c.s);
        assertEquals(l1.c.c.s, l1n.c.c.s);
        assertEquals(l1.c.c.c.s, l1n.c.c.c.s);
        assertEquals(l1.c.c.c.i, l1n.c.c.c.i);
        assertEquals(l1.c.c.c.d, l1n.c.c.c.d, 0.001);
    }

    @Test
    public void importMultiNonDefaultCombo() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("importConfig.xml");
        L1Configurable l1 = (L1Configurable) cm1.lookup("l1");
        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.importConfigurable(l1, "l11");
        cm2.save(f);
        cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        L1Configurable l1n = (L1Configurable) cm2.lookup("l11");
        assertEquals(l1.s, l1n.s);
        assertEquals(l1.c.s, l1n.c.s);
        assertEquals(l1.c.c.s, l1n.c.c.s);
        assertEquals(l1.c.c.c.s, l1n.c.c.c.s);
        assertEquals(l1.c.c.c.i, l1n.c.c.c.i);
        assertEquals(l1.c.c.c.d, l1n.c.c.c.d, 0.001);
    }

    @Test
    public void importStringList() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("stringListConfig.xml");
        StringListConfigurable sl1 = (StringListConfigurable) cm1.lookup(
                "listTest");
        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.importConfigurable(sl1, "listTest");
        cm2.save(f);
        cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        StringListConfigurable sl2 = (StringListConfigurable) cm1.lookup(
                "listTest");

        for (int i = 0; i < sl1.strings.size(); i++) {
            assertEquals(sl1.strings.get(i), sl2.strings.get(i));
        }
    }

    @Test
    public void importSimpleComponentList() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("importConfig.xml");
        ListConfigurable lc1 = (ListConfigurable) cm1.lookup("simpleList");
        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.importConfigurable(lc1, "simpleList");
        cm2.save(f);
        cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        ListConfigurable lc2 = (ListConfigurable) cm2.lookup("simpleList");
        SimpleConfigurable sc1 = (SimpleConfigurable) lc1.list.get(0);
        SimpleConfigurable sc2 = (SimpleConfigurable) lc2.list.get(0);
        assertEquals(sc1.simple, sc2.simple);

        StringConfigurable stc1 = (StringConfigurable) lc1.list.get(1);
        StringConfigurable stc2 = (StringConfigurable) lc2.list.get(1);
        assertEquals(stc1.one, stc2.one);
        assertEquals(stc1.two, stc2.two);
        assertEquals(stc1.three, stc2.three);
    }

    @Test
    public void importSingleEmbeddedComponentList() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("importConfig.xml");
        ListConfigurable lc1 = (ListConfigurable) cm1.lookup("singleEmbeddedList");
        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.importConfigurable(lc1, "singleEmbeddedList");
        cm2.save(f);
        cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        ListConfigurable lc2 = (ListConfigurable) cm2.lookup("singleEmbeddedList");

        StringConfigurable stc1 = (StringConfigurable) lc1.list.get(0);
        StringConfigurable stc2 = (StringConfigurable) lc2.list.get(0);
        assertEquals(stc1.one, stc2.one);
        assertEquals(stc1.two, stc2.two);
        assertEquals(stc1.three, stc2.three);
        L1Configurable l1 = (L1Configurable) lc1.list.get(1);
        L1Configurable l1n = (L1Configurable) lc2.list.get(1);
        assertEquals(l1.s, l1n.s);
        assertEquals(l1.c.s, l1n.c.s);
        assertEquals(l1.c.c.s, l1n.c.c.s);
        assertEquals(l1.c.c.c.s, l1n.c.c.c.s);
        assertEquals(l1.c.c.c.i, l1n.c.c.c.i);
        assertEquals(l1.c.c.c.d, l1n.c.c.c.d, 0.001);
    }

    @Test
    public void importMultiEmbeddedComponentList() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("importConfig.xml");
        ListConfigurable lc1 = (ListConfigurable) cm1.lookup("multiEmbeddedList");
        ConfigurationManager cm2 = new ConfigurationManager();
        cm2.importConfigurable(lc1, "multiEmbeddedList");
        cm2.save(f);
        cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        ListConfigurable lc2 = (ListConfigurable) cm2.lookup("multiEmbeddedList");

        L1Configurable l1 = (L1Configurable) lc1.list.get(0);
        L1Configurable l1n = (L1Configurable) lc2.list.get(0);
        assertEquals(l1.s, l1n.s);
        assertEquals(l1.c.s, l1n.c.s);
        assertEquals(l1.c.c.s, l1n.c.c.s);
        assertEquals(l1.c.c.c.s, l1n.c.c.c.s);
        assertEquals(l1.c.c.c.i, l1n.c.c.c.i);
        assertEquals(l1.c.c.c.d, l1n.c.c.c.d, 0.001);

        l1 = (L1Configurable) lc1.list.get(1);
        l1n = (L1Configurable) lc2.list.get(1);
        assertEquals(l1.s, l1n.s);
        assertEquals(l1.c.s, l1n.c.s);
        assertEquals(l1.c.c.s, l1n.c.c.s);
        assertEquals(l1.c.c.c.s, l1n.c.c.c.s);
        assertEquals(l1.c.c.c.i, l1n.c.c.c.i);
        assertEquals(l1.c.c.c.d, l1n.c.c.c.d, 0.001);
    }

    @Test
    public void testNullImport() {
        ConfigurationManager cm = new ConfigurationManager();

        // Test import when mandatory field is non-null
        ListConfig l = new ListConfig();
        l.doubleList = Arrays.asList(1.0,2.0,3.0);
        l.stringList = null;
        l.stringConfigurableList = null;

        cm.importConfigurable(l,"list-config");
        ListConfig newL = (ListConfig) cm.lookup("list-config");
        assertEquals(l.doubleList, newL.doubleList);
        assertNull(newL.stringList);
        assertNull(newL.stringConfigurableList);

        // Test import when mandatory field is null, should throw
        ListConfig nullMandatory = new ListConfig();
        nullMandatory.doubleList = null;
        nullMandatory.stringConfigurableList = null;
        nullMandatory.stringList = Arrays.asList("foo","bar","baz","quux");

        assertThrows(PropertyException.class,() -> cm.importConfigurable(nullMandatory,"null-list-config"));
    }
}