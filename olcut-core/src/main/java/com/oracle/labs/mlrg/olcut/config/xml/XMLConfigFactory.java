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

package com.oracle.labs.mlrg.olcut.config.xml;

import com.oracle.labs.mlrg.olcut.config.io.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.io.FileFormatFactory;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.io.URLLoader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;

import java.io.OutputStream;
import java.util.Map;

/**
 *
 */
public class XMLConfigFactory implements FileFormatFactory {

    private final XMLOutputFactory factory = XMLOutputFactory.newFactory();

    /**
     * Creates a new XMLConfigFactory.
     */
    public XMLConfigFactory() {}

    @Override
    public String getExtension() {
        return "xml";
    }

    @Override
    public ConfigLoader getLoader(URLLoader parent, Map<String, ConfigurationData> rpdMap, Map<String, ConfigurationData> existingRPD, Map<String, SerializedObject> serializedObjects, GlobalProperties globalProperties) throws ConfigLoaderException {
        try {
            return new SAXLoader(parent, rpdMap, existingRPD, serializedObjects, globalProperties);
        } catch (SAXException | ParserConfigurationException e) {
            throw new ConfigLoaderException(e);
        }
    }

    @Override
    public ConfigWriter getWriter(OutputStream writer) throws ConfigWriterException {
        try {
            XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer, "utf-8");
            return new XMLConfigWriter(xmlWriter);
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }
}
