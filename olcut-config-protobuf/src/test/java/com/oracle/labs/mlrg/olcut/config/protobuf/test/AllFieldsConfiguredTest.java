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

package com.oracle.labs.mlrg.olcut.config.protobuf.test;

import com.oracle.labs.mlrg.olcut.config.protobuf.ProtoConfigFactory;
import com.oracle.labs.mlrg.olcut.config.protobuf.ProtoTxtConfigFactory;
import com.oracle.labs.mlrg.olcut.test.config.AllFieldsConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.labs.mlrg.olcut.config.ConfigurationManager.createModuleResourceString;
import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests reading and writing all valid fields from a config file.
 */
public class AllFieldsConfiguredTest {

    private File binaryFile;
    private File textFile;

    @BeforeAll
    public static void setup() {
        Logger logger = Logger.getLogger(PropertySheet.class.getName());
        logger.setLevel(Level.SEVERE);
    }

    @BeforeEach
    public void setUp() throws IOException {
        ConfigurationManager.addFileFormatFactory(new ProtoConfigFactory());
        ConfigurationManager.addFileFormatFactory(new ProtoTxtConfigFactory());
        binaryFile = File.createTempFile("all-config", ".pb");
        binaryFile.deleteOnExit();
        textFile = File.createTempFile("all-config", ".pbtxt");
        textFile.deleteOnExit();
    }

    @Test
    public void loadConfig() {
        ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "allConfig.pb"));
        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup("all-config");
        assertNotNull(ac, "Failed to load all-config from binary");
        cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "allConfig.pbtxt"));
        ac = (AllFieldsConfigurable) cm.lookup("all-config");
        assertNotNull(ac, "Failed to load all-config from text");
    }

    @Test
    public void saveConfig() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager(createModuleResourceString(this.getClass(), "allConfig.pb"));
        AllFieldsConfigurable ac1 = (AllFieldsConfigurable) cm1.lookup("all-config");
        cm1.save(binaryFile, true);
        cm1.save(textFile, true);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(binaryFile.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        assertEquals(ac1,ac2,"Two all configs aren't equal");
        ConfigurationManager cm3 = new ConfigurationManager(replaceBackSlashes(textFile.toString()));
        AllFieldsConfigurable ac3 = (AllFieldsConfigurable) cm3.lookup("all-config");
        assertEquals(ac1,ac3,"Two all configs aren't equal");
    }

    @Test
    public void generateConfig() throws IOException {
        AllFieldsConfigurable ac = AllFieldsConfigurable.generateConfigurable();
        ConfigurationManager cm1 = new ConfigurationManager();
        cm1.importConfigurable(ac,"all-config");
        cm1.save(binaryFile);
        cm1.save(textFile);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(binaryFile.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        assertEquals(ac,ac2, "Imported config not equal to generated object");
        ConfigurationManager cm3 = new ConfigurationManager(replaceBackSlashes(textFile.toString()));
        AllFieldsConfigurable ac3 = (AllFieldsConfigurable) cm3.lookup("all-config");
        assertEquals(ac,ac3, "Imported config not equal to generated object");
    }

}
