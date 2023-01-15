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

package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.test.BasicConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.test.ListConfig;
import com.oracle.labs.mlrg.olcut.config.property.Property;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.SetConfig;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.config.test.StringConfigurable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SaveTest {

    private File f;

    @BeforeEach
    public void setUp() throws IOException {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
        f = File.createTempFile("config", ".json");
        f.deleteOnExit();
    }

    @Test
    public void saveAllWithInstantiationGeneric() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("genericConfig.json");
        SetConfig s1 = (SetConfig) cm1.lookup("correctSetConfig");
        cm1.save(f, true);
        assertEquals(3, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        SetConfig s2 = (SetConfig) cm2.lookup("correctSetConfig");
        assertEquals(s1, s2);
        ListConfig l1 = (ListConfig) cm1.lookup("correctListConfig");
        ListConfig l2 = (ListConfig) cm2.lookup("correctListConfig");
        assertEquals(l1, l2);
    }

    @Test
    public void saveAllWithNoInstantiationGeneric() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("genericConfig.json");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        SetConfig s1 = (SetConfig) cm1.lookup("correctSetConfig");
        SetConfig s2 = (SetConfig) cm2.lookup("correctSetConfig");
        assertEquals(s1, s2);
        ListConfig l1 = (ListConfig) cm1.lookup("correctListConfig");
        ListConfig l2 = (ListConfig) cm2.lookup("correctListConfig");
        assertEquals(l1, l2);
    }

    @Test
    public void saveAllWithInstantiation() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.json");
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        cm1.save(f, true);
        assertEquals(1, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertEquals(bc1, bc2);
        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }

    @Test
    public void saveAllWithNoInstantiation() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.json");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertEquals(bc1, bc2);
        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }
    
    @Test
    public void saveAllWithInstantiationAndAddition() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.json");
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        Map<String, Property> m = new HashMap<>();
        m.put("s", new SimpleProperty("foo"));
        m.put("i", new SimpleProperty(""+7));
        m.put("d", new SimpleProperty(""+2.71));
        cm1.addConfiguration(new ConfigurationData("c",BasicConfigurable.class.getName(),m));
        cm1.save(f, true);
        assertEquals(1, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertEquals(bc1, bc2);
        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
        
        bc2 = (BasicConfigurable) cm2.lookup("c");
        assertEquals("foo", bc2.s);
        assertEquals(7, bc2.i);
        assertEquals(2.71, bc2.d, 0.01);
    }

    @Test
    public void saveAllWithoutInstantiationAndAddition() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.json");
        Map<String, Property> m = new HashMap<>();
        m.put("s", new SimpleProperty("foo"));
        m.put("i", new SimpleProperty(""+7));
        m.put("d", new SimpleProperty(""+2.71));
        cm1.addConfiguration(new ConfigurationData("c",BasicConfigurable.class.getName(),m));
        cm1.save(f, true);
        assertEquals(0, cm1.getNumInstantiated());
        
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        BasicConfigurable bc2 = (BasicConfigurable) cm2.lookup("a");
        assertEquals(bc1, bc2);
        
        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);

        bc2 = (BasicConfigurable) cm2.lookup("c");
        assertEquals("foo", bc2.s);
        assertEquals(7, bc2.i);
        assertEquals(2.71, bc2.d, 0.01);
    }

    @Test
    public void saveAllWithInstantiationAndRemoval() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.json");
        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("a");
        cm1.removeConfigurable("a");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumInstantiated());
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        BasicConfigurable bc2;
        try {
            bc2 = (BasicConfigurable) cm2.lookup("a");
            fail("Found removed component");
        } catch (PropertyException e) {}

        bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }

    @Test
    public void saveAllWithoutInstantiationAndRemoval() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.json");
        cm1.removeConfigurable("a");
        cm1.save(f, true);
        assertEquals(0, cm1.getNumInstantiated());

        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        BasicConfigurable bc2;
        try{
            bc2 = (BasicConfigurable) cm2.lookup("a");
            fail("Found a removed component");
        } catch (PropertyException e) { }

        BasicConfigurable bc1 = (BasicConfigurable) cm1.lookup("b");
        bc2 = (BasicConfigurable) cm2.lookup("b");
        assertEquals(bc1, bc2);
    }

    @Test
    public void removeProgramaticallyAddedUninstantiated() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("basicConfig.json");
        Map<String, Property> m = new HashMap<>();
        m.put("s", new SimpleProperty("foo"));
        m.put("i", new SimpleProperty(""+7));
        m.put("d", new SimpleProperty(""+2.71));
        cm1.addConfiguration(new ConfigurationData("c",BasicConfigurable.class.getName(),m));
        boolean removed = cm1.removeConfigurable("c");
        assertTrue(removed);
        cm1.save(f, false);
        try{
            BasicConfigurable bc = (BasicConfigurable) cm1.lookup("c");
            fail("Found a removed component");
        } catch (PropertyException e) { }

        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        try{
            BasicConfigurable bc = (BasicConfigurable) cm2.lookup("c");
            fail("Found a removed component");
        } catch (PropertyException e) { }
    }

    @Test
    public void addNewConfigProgramatically() throws IOException {
        StringConfigurable sc = new StringConfigurable("foo", "bar", "quux");

        ConfigurationManager cm = new ConfigurationManager();
        cm.importConfigurable(sc, "testStringConfig");
        cm.save(f, false);

        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        StringConfigurable sc2 = (StringConfigurable) cm2.lookup("testStringConfig");
        assertEquals(sc,sc2);
    }

    @Test
    public void loadNastyStrings() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("nastyStringConfig.json");
        StringConfigurable sc = (StringConfigurable) cm.lookup(
                "nastyStringTest");
        assertEquals("([^a-z0-9_!#$%&*@＠]|^|RT:?)(@＠+)([a-z0-9_]{1,20})(/[a-z][a-z0-9_\\\\-]{0,24})?", sc.one);
        assertEquals("@＠", sc.two);
        assertEquals("&&", sc.three);
    }

    @Test
    public void saveNastyStrings() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager();

        StringConfigurable sc1 = new StringConfigurable("([^a-z0-9_!#$%&*@＠]|^|RT:?)(@＠+)([a-z0-9_]{1,20})(/[a-z][a-z0-9_\\\\-]{0,24})?","@＠","&&");

        cm1.importConfigurable(sc1,"nastyString");
        cm1.save(f);

        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        StringConfigurable sc2 = (StringConfigurable) cm2.lookup("nastyString");
        assertEquals(sc1,sc2);
    }
}
