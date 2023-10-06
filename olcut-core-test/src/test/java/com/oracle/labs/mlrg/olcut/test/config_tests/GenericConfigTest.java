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

package com.oracle.labs.mlrg.olcut.test.config_tests;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.test.config.Ape;
import com.oracle.labs.mlrg.olcut.test.config.ArrayStringConfigurable;
import com.oracle.labs.mlrg.olcut.test.config.Barbary;
import com.oracle.labs.mlrg.olcut.test.config.Chimp;
import com.oracle.labs.mlrg.olcut.test.config.Gorilla;
import com.oracle.labs.mlrg.olcut.test.config.InvalidListConfigurable;
import com.oracle.labs.mlrg.olcut.test.config.ListConfig;
import com.oracle.labs.mlrg.olcut.test.config.Monkey;
import com.oracle.labs.mlrg.olcut.test.config.Orangutan;
import com.oracle.labs.mlrg.olcut.test.config.Rhesus;
import com.oracle.labs.mlrg.olcut.test.config.SetConfig;
import com.oracle.labs.mlrg.olcut.test.config.StringConfigurable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.config.ConfigurationManager.createModuleResourceString;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class GenericConfigTest {

    @SuppressWarnings("unchecked")//Looking up a specific class via it's full name
    @Test
    public void lookupAllTest() throws ClassNotFoundException {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "genericConfig.xml"));

        Class<Monkey> clazz = (Class<Monkey>) Class.forName("com.oracle.labs.mlrg.olcut.test.config.Monkey");
        List<Monkey> monkeys = cm.lookupAll(clazz);

        assertEquals(5, monkeys.size(), "Didn't find all the MonkeyConfigurable classes");

        Class<Ape> apeClazz = (Class<Ape>) Class.forName("com.oracle.labs.mlrg.olcut.test.config.Ape");
        List<Ape> apes = cm.lookupAll(apeClazz);

        assertEquals(3, apes.size(), "Didn't find all the Ape classes");
    }

    @SuppressWarnings("unchecked")//Looking up a specific class via it's full name
    @Test
    public void lookupAllMapTest() throws ClassNotFoundException {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "genericConfig.xml"));

        Class<Monkey> clazz = (Class<Monkey>) Class.forName("com.oracle.labs.mlrg.olcut.test.config.Monkey");
        Map<String,Monkey> monkeys = cm.lookupAllMap(clazz);

        assertEquals(5, monkeys.size(), "Didn't find all the MonkeyConfigurable classes");
        assertTrue(monkeys.get("monkey-one") instanceof Gorilla);
        assertTrue(monkeys.get("monkey-two") instanceof Chimp);
        assertTrue(monkeys.get("monkey-three") instanceof Orangutan);
        assertTrue(monkeys.get("monkey-four") instanceof Rhesus);
        assertTrue(monkeys.get("monkey-five") instanceof Barbary);

        Class<Ape> apeClazz = (Class<Ape>) Class.forName("com.oracle.labs.mlrg.olcut.test.config.Ape");
        Map<String,Ape> apes = cm.lookupAllMap(apeClazz);

        assertEquals(3, apes.size(), "Didn't find all the Ape classes");
        assertTrue(apes.get("monkey-one") instanceof Gorilla);
        assertTrue(apes.get("monkey-two") instanceof Chimp);
        assertTrue(apes.get("monkey-three") instanceof Orangutan);

        Class<Gorilla> gorillaClazz = (Class<Gorilla>) Class.forName("com.oracle.labs.mlrg.olcut.test.config.Gorilla");
        Map<String,Gorilla> gorillas = cm.lookupAllMap(gorillaClazz);

        assertEquals(1,gorillas.size(),"Didn't find the Gorilla instance");
        assertTrue(gorillas.get("monkey-one") instanceof Gorilla);

        Class<ArrayStringConfigurable> arrStrConfClazz = (Class<ArrayStringConfigurable>) Class.forName("com.oracle.labs.mlrg.olcut.test.config.ArrayStringConfigurable");
        Map<String, ArrayStringConfigurable> arrays = cm.lookupAllMap(arrStrConfClazz);

        assertTrue(arrays.isEmpty());
    }

    @Test
    public void correctListConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "genericConfig.xml"));
        ListConfig s = (ListConfig) cm.lookup("correctListConfig");

        Assertions.assertEquals(4,s.stringList.size(), "StringList has an incorrect number of values");
        Assertions.assertTrue(s.stringList.contains("5.5"),"StringList missing values");
        Assertions.assertTrue(s.stringList.contains("3.14"), "StringList missing values");
        Assertions.assertTrue(s.stringList.contains("2.79"), "StringList missing values");
        Assertions.assertFalse(s.stringList.contains("1000000.0"), "StringList contains incorrect value");

        Assertions.assertEquals(4,s.doubleList.size(), "DoubleList has an incorrect number of values");
        Assertions.assertTrue(s.doubleList.contains(5.5), "DoubleList missing values");
        Assertions.assertTrue(s.doubleList.contains(3.14), "DoubleList missing values");
        Assertions.assertTrue(s.doubleList.contains(2.79), "DoubleList missing values");
        Assertions.assertFalse(s.doubleList.contains(1000000.0), "DoubleList contains incorrect values");

        Assertions.assertEquals(2,s.stringConfigurableList.size(), "StringConfigList has an incorrect number of values");
        Assertions.assertTrue(s.stringConfigurableList.contains(new StringConfigurable("dragons","wyverns","wyrms")), "StringConfigList missing values");
        Assertions.assertTrue(s.stringConfigurableList.contains(new StringConfigurable("jedi","sith","scoundrels")), "StringConfigList missing values");
        Assertions.assertFalse(s.stringConfigurableList.contains(new StringConfigurable("sheep","cows","pigs")), "StringConfigList contains incorrect values");
    }

    @Test
    public void incorrectListConfig() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "genericConfig.xml"));
            ListConfig l = (ListConfig) cm.lookup("incorrectListConfig");
        });
    }

    @Test
    public void correctSetConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "genericConfig.xml"));
        SetConfig s = (SetConfig) cm.lookup("correctSetConfig");

        assertEquals(3,s.stringSet.size(), "StringSet has an incorrect number of values");
        assertTrue(s.stringSet.contains("5.5"), "StringSet missing values");
        assertTrue(s.stringSet.contains("3.14"), "StringSet missing values");
        assertTrue(s.stringSet.contains("2.79"), "StringSet missing values");
        assertFalse(s.stringSet.contains("1000000.0"), "StringSet contains incorrect value");

        assertEquals(3,s.doubleSet.size(), "DoubleSet has an incorrect number of values");
        assertTrue(s.doubleSet.contains(5.5), "DoubleSet missing values");
        assertTrue(s.doubleSet.contains(3.14), "DoubleSet missing values");
        assertTrue(s.doubleSet.contains(2.79), "DoubleSet missing values");
        assertFalse(s.doubleSet.contains(1000000.0), "DoubleSet contains incorrect values");

        assertEquals(2,s.stringConfigurableSet.size(), "StringConfigSet has an incorrect number of values");
        assertTrue(s.stringConfigurableSet.contains(new StringConfigurable("dragons","wyverns","wyrms")), "StringConfigSet missing values");
        assertTrue(s.stringConfigurableSet.contains(new StringConfigurable("jedi","sith","scoundrels")), "StringConfigSet missing values");
        assertFalse(s.stringConfigurableSet.contains(new StringConfigurable("sheep","cows","pigs")), "StringConfigSet contains incorrect values");
    }

    @Test
    public void incorrectSetConfig() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "genericConfig.xml"));
            SetConfig s = (SetConfig) cm.lookup("incorrectSetConfig");
        });
    }

    @Test
    public void invalidListConfig() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "invalidGenericConfig.xml"));
        assertThrows(PropertyException.class, () -> {
            InvalidListConfigurable l = (InvalidListConfigurable) cm.lookup("test");
        });
    }
}
