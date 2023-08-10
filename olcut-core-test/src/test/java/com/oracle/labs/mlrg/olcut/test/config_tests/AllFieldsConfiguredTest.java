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

package com.oracle.labs.mlrg.olcut.test.config_tests;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.oracle.labs.mlrg.olcut.test.config.AllFieldsConfigurable;
import com.oracle.labs.mlrg.olcut.test.config.StringConfigurable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.config.ConfigurationManager.createModuleResourceString;
import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests reading and writing all valid fields from a config file.
 */
public class AllFieldsConfiguredTest {

    private File f;

    @BeforeAll
    public static void setup() {
        Logger logger = Logger.getLogger(PropertySheet.class.getName());
        logger.setLevel(Level.SEVERE);
    }

    @BeforeEach
    public void setUp() throws IOException {
        f = File.createTempFile("all-config", ".xml");
        f.deleteOnExit();
    }

    @Test
    public void loadConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "allConfig.xml"));
        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup("all-config");
        Assertions.assertNotNull(ac, "Failed to load all-config");
    }

    @Test
    public void saveConfig() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager(createModuleResourceString(this.getClass(), "allConfig.xml"));
        AllFieldsConfigurable ac1 = (AllFieldsConfigurable) cm1.lookup("all-config");
        cm1.save(f, true);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        Assertions.assertEquals(ac1,ac2,"Two all configs aren't equal");
    }

    @Test
    public void generateConfig() throws IOException {
        AllFieldsConfigurable ac = AllFieldsConfigurable.generateConfigurable();
        ConfigurationManager cm1 = new ConfigurationManager();
        cm1.importConfigurable(ac,"all-config");
        cm1.save(f);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        Assertions.assertEquals(ac,ac2,"Imported config not equal to generated object");
    }

    @Test
    public void overrideTest() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "allConfig.xml"));

        // Override various properties.
        cm.overrideConfigurableProperty("all-config","boolField",new SimpleProperty("false"));
        cm.overrideConfigurableProperty("all-config","doubleArrayField", ListProperty.createFromStringList(Arrays.asList("3.14","2.77","1.0")));
        // This rearranges the elements of listConfigurableSubclassField
        cm.overrideConfigurableProperty("all-config","listConfigurableSubclassField", ListProperty.createFromStringList(Arrays.asList("second-configurable","first-configurable")));
        Map<String,String> newMap = new HashMap<>();
        newMap.put("one","1.0");
        newMap.put("two","2.0");
        cm.overrideConfigurableProperty("all-config","mapDoubleField", MapProperty.createFromStringMap(newMap));

        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup("all-config");

        Assertions.assertEquals(false,ac.boolField);
        Assertions.assertArrayEquals(new double[]{3.14,2.77,1.0},ac.doubleArrayField,1e-10);
        Assertions.assertEquals(new StringConfigurable("alpha","beta","gamma"),ac.listConfigurableSubclassField.get(0));
        Assertions.assertEquals(new StringConfigurable("A","B","C"),ac.listConfigurableSubclassField.get(1));
        Assertions.assertEquals(1.0,ac.mapDoubleField.get("one"),1e-10);
        Assertions.assertEquals(2.0,ac.mapDoubleField.get("two"),1e-10);
    }

}
