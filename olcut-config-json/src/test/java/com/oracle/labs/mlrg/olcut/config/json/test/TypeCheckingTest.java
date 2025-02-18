/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config.json.test;

import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.json.JsonConfigFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.oracle.labs.mlrg.olcut.config.ConfigurationManager.createModuleResourceString;
import static org.junit.jupiter.api.Assertions.fail;

public class TypeCheckingTest {

    @BeforeAll
    public static void setUp() {
        ConfigurationManager.addFileFormatFactory(new JsonConfigFactory());
    }

    @Test
    public void arrayTest() throws IOException {
        try {
            ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "typeCheckingListConfig.json"));
            fail("Should have thrown ConfigLoaderException");
        } catch (ConfigLoaderException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Invalid value"));
        }
    }

    @Test
    public void propertyTest() throws IOException {
        try {
            ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "typeCheckingPropertyConfig.json"));
            fail("Should have thrown ConfigLoaderException");
        } catch (ConfigLoaderException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Invalid value"));
        }
    }

    @Test
    public void mapTest() throws IOException {
        try {
            ConfigurationManager cm = new ConfigurationManager(createModuleResourceString(this.getClass(), "typeCheckingMapConfig.json"));
            fail("Should have thrown ConfigLoaderException");
        } catch (ConfigLoaderException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Invalid value"));
        }
    }

}
