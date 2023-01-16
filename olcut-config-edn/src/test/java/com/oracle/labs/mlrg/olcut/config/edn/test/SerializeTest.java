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
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.edn.EdnConfigFactory;
import com.oracle.labs.mlrg.olcut.test.config.StringConfigurable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class SerializeTest {

    private static final Logger logger = Logger.getLogger(SerializeTest.class.getName());

    private Path serPath;

    @BeforeAll
    public static void setUpClass() throws IOException {
        ConfigurationManager.addFileFormatFactory(new EdnConfigFactory());
    }


    @BeforeEach
    public void setUp() throws IOException {
        serPath = Files.createTempFile("ac", "ser");
        //
        // I just want the file name, not the actual file!
        Files.delete(serPath);
    }

    @AfterEach
    public void tearDown() throws IOException {
        try {
            Files.delete(serPath);
        }catch(Exception e) {}
    }

    @Test
    public void deserializeComponent() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(this.getClass().getName()+"|stringConfig.edn");
        cm.setGlobalProperty("serFile", serPath.toString());
        StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
            oos.writeObject(ac);
        }
        cm = new ConfigurationManager(this.getClass().getName()+"|stringConfig.edn");
        cm.setGlobalProperty("serFile", serPath.toString());
        ac = (StringConfigurable) cm.lookup("ac");
        assertEquals(ac.one, "one");
        assertEquals(ac.two, "b");
        assertEquals(ac.three, "c");
    }

    /**
     * Tests whether when we de-serialize a component and then ask for it again
     * that we're not deserializing it a second time.
     */
    @Test
    public void deserializeComponentAndReuse() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(this.getClass().getName()+"|stringConfig.edn");
        cm.setGlobalProperty("serFile", serPath.toString());
        StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
            oos.writeObject(ac);
        }
        cm = new ConfigurationManager(this.getClass().getName()+"|stringConfig.edn");
        cm.setGlobalProperty("serFile", serPath.toString());
        ac = (StringConfigurable) cm.lookup("ac");
        assertEquals(ac.one, "one");
        assertEquals(ac.two, "b");
        assertEquals(ac.three, "c");
        ac.one = "two";
        ac.three = "three";
        ac = (StringConfigurable) cm.lookup("ac");
        assertEquals(ac.one, "two");
        assertEquals(ac.two, "b");
        assertEquals(ac.three, "three");
        
    }

    @Test
    public void deserializeObject() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(this.getClass().getName()+"|stringConfig.edn");
        cm.setGlobalProperty("serFile", serPath.toString());
        StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
            oos.writeObject(ac);
        }
        StringConfigurable acs = (StringConfigurable) cm.lookupSerializedObject("acs");
        assertEquals(acs.one, "one");
        assertEquals(acs.two, "b");
        assertEquals(acs.three, "c");
    }

    @Test
    public void deserializeObjectAndReuse() throws IOException {
        ConfigurationManager cm = new ConfigurationManager(this.getClass().getName()+"|stringConfig.edn");
        cm.setGlobalProperty("serFile", serPath.toString());
        StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
        ac.one = "one";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
            oos.writeObject(ac);
        }
        StringConfigurable acs = (StringConfigurable) cm.lookupSerializedObject("acs");
        assertEquals(acs.one, "one");
        assertEquals(acs.two, "b");
        assertEquals(acs.three, "c");
        acs.one = "two";
        acs.three = "three";
        acs = (StringConfigurable) cm.lookupSerializedObject("acs");
        assertEquals(acs.one, "two");
        assertEquals(acs.two, "b");
        assertEquals(acs.three, "three");
    }

    @Test
    public void checkBadSerialisedClass() throws IOException {
        assertThrows(PropertyException.class, () -> {
            ConfigurationManager cm = new ConfigurationManager(this.getClass().getName()+"|stringConfig.edn");
            cm.setGlobalProperty("serFile", serPath.toString());
            StringConfigurable ac = (StringConfigurable) cm.lookup("ac");
            ac.one = "one";
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serPath.toFile()))) {
                oos.writeObject(ac);
            }
            StringConfigurable acs = (StringConfigurable) cm.lookupSerializedObject("badClass");
        }, "Should have thrown PropertyException for an unknown class file");
    }
}
