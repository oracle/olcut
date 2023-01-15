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

import com.oracle.labs.mlrg.olcut.config.test.BasicConfigurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.property.Property;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.config.test.StringConfigurable;
import com.oracle.labs.mlrg.olcut.util.LabsLogFormatter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.labs.mlrg.olcut.util.IOUtil.replaceBackSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class ProgrammaticConfigurableTest {

    private static final Logger logger = Logger.getLogger(ProgrammaticConfigurableTest.class.getName());

    @BeforeAll
    public static void setUpClass() throws Exception {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
        LabsLogFormatter.setAllLogFormatters(Level.FINER);
    }

    /**
     * Tests adding a configurable with the default properties.
     */
    @Test
    public void addDefaultStringConfigurable() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
        cm.addConfiguration(StringConfigurable.class, "c");
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("", sc.one);
        assertEquals("", sc.two);
        assertEquals("", sc.three);
    }

    /**
     * Tests adding a configurable with explicit properties.
     */
    @Test
    public void addStringConfigurable() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
        Map<String, Property> m = new HashMap<>();
        for(String s : new String[] {"one", "two", "three"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfiguration(new ConfigurationData("c",StringConfigurable.class.getName(),m));
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("one", sc.one);
        assertEquals("two", sc.two);
        assertEquals("three", sc.three);
    }

    /**
     * Tests adding a configurable with some explicit properties and some
     * default ones.
     */
    @Test
    public void addPartialStringConfigurable() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
        Map<String,Property> m = new HashMap<>();
        for(String s : new String[] {"one"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfiguration(new ConfigurationData("c",StringConfigurable.class.getName(),m));
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("one", sc.one);
        assertEquals("", sc.two);
        assertEquals("", sc.three);
        m.clear();
        for(String s : new String[]{"one", "three"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfiguration(new ConfigurationData("d",StringConfigurable.class.getName(),m));
        sc = (StringConfigurable) cm.lookup("d");
        assertEquals("one", sc.one);
        assertEquals("", sc.two);
        assertEquals("three", sc.three);
    }

    /**
     * Tests adding a configurable with an incorrect property type.
     */
    @Test
    public void addConfigurableWithBadProperty() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("basicConfig.edn");
            Map<String,Property> m = new HashMap<>();
            m.put("s", new SimpleProperty("one"));
            m.put("i", new SimpleProperty("two"));
            cm.addConfiguration(new ConfigurationData("c",BasicConfigurable.class.getName(),m));
            BasicConfigurable bc = (BasicConfigurable) cm.lookup("c");
            assertEquals("one", bc.s);
            assertEquals(2, bc.i);
        });
    }

    /**
     * Tests adding a configurable with an existing name, which should not throw
     * an exception if it the existing configurable hasn't been instantiated.  This
     * is just like overriding one configuration file with another.
     */
    @Test
    public void addAlreadyNamedStringConfigurable() throws IOException {
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
        Map<String,Property> m = new HashMap<>();
        for(String s : new String[] {"one", "two", "three"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfiguration(new ConfigurationData("a",StringConfigurable.class.getName(),m));
        StringConfigurable sc = (StringConfigurable) cm.lookup("a");
        assertEquals("one", sc.one);
        assertEquals("two", sc.two);
        assertEquals("three", sc.three);
    }

    /**
     * Tests adding a configurable with an existing name, which should throw
     * an exception if it the existing configurable hasn't been instantiated.
     */
    @Test
    public void addAlreadyNamedAndInstatiatedStringConfigurable() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
            StringConfigurable sc = (StringConfigurable) cm.lookup("a");
            Map<String,Property> m = new HashMap<>();
            for(String s : new String[] {"one", "two", "three"}) {
                m.put(s, new SimpleProperty(s));
            }
            cm.addConfiguration(new ConfigurationData("a",StringConfigurable.class.getName(),m));
            sc = (StringConfigurable) cm.lookup("a");
        });
    }

    @Test
    public void testWriting() throws IOException {

        //
        // Add a component.
        ConfigurationManager cm = new ConfigurationManager("stringConfig.edn");
        Map<String, Property> m = new HashMap<>();
        for(String s : new String[]{"one", "two", "three"}) {
            m.put(s, new SimpleProperty(s));
        }
        cm.addConfiguration(new ConfigurationData("c",StringConfigurable.class.getName(),m));
        StringConfigurable c = (StringConfigurable) cm.lookup("c");

        //
        // Write the file.
        File f = File.createTempFile("config", ".edn");
        cm.save(f);

        //
        // Re-read the file.
        cm = new ConfigurationManager(replaceBackSlashes(f.toString()));
        StringConfigurable sc = (StringConfigurable) cm.lookup("c");
        assertEquals("one", sc.one);
        assertEquals("two", sc.two);
        assertEquals("three", sc.three);
        //logger.info(String.format("f: %s", f));
        f.deleteOnExit();
    }
}
