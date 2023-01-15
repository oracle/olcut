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

package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.test.AllFieldsConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
        f = File.createTempFile("all-config", ".edn");
        f.deleteOnExit();
    }

    @Test
    public void loadConfig() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("allConfig.edn");
        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup("all-config");
        assertNotNull(ac, "Failed to load all-config");
    }

    @Test
    public void saveConfig() throws IOException {
        ConfigurationManager cm1 = new ConfigurationManager("allConfig.edn");
        AllFieldsConfigurable ac1 = (AllFieldsConfigurable) cm1.lookup("all-config");
        cm1.save(f, true);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        assertEquals(ac1,ac2, "Two all configs aren't equal");
    }

    @Test
    public void generateConfig() throws IOException {
        AllFieldsConfigurable ac = com.oracle.labs.mlrg.olcut.config.AllFieldsConfiguredTest.generateConfigurable();
        ConfigurationManager cm1 = new ConfigurationManager();
        cm1.importConfigurable(ac,"all-config");
        cm1.save(f);
        ConfigurationManager cm2 = new ConfigurationManager(replaceBackSlashes(f.toString()));
        AllFieldsConfigurable ac2 = (AllFieldsConfigurable) cm2.lookup("all-config");
        assertEquals(ac,ac2, "Imported config not equal to generated object");
    }
}
