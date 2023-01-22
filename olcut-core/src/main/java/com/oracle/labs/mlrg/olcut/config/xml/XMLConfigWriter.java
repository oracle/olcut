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
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.Property;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link ConfigWriter} for XML format configuration files.
 * <p>
 * This class is not thread-safe.
 */
public class XMLConfigWriter implements ConfigWriter {
    private final XMLStreamWriter writer;

    /**
     * Constructs an XMLConfigWriter using the supplied XMLStreamWriter.
     * @param writer The XML writer to use.
     */
    public XMLConfigWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    @Override
    public void writeStartDocument() throws ConfigWriterException {
        try {
            writer.writeStartDocument("utf-8", "1.0");
            writer.writeCharacters(System.lineSeparator());
            writer.writeComment("OLCUT configuration file");
            writer.writeCharacters(System.lineSeparator());
            writer.writeStartElement(ConfigLoader.CONFIG);
            writer.writeCharacters(System.lineSeparator());
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeEndDocument() throws ConfigWriterException {
        try {
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeGlobalProperties(Map<String,String> props) throws ConfigWriterException {
        try {
            for (Map.Entry<String, String> e : props.entrySet()) {
                writer.writeEmptyElement(ConfigLoader.PROPERTY);
                writer.writeAttribute(ConfigLoader.NAME, e.getKey());
                writer.writeAttribute(ConfigLoader.VALUE, e.getValue());
                writer.writeCharacters(System.lineSeparator());
            }
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeSerializedObjects(Map<String, SerializedObject> map) throws ConfigWriterException {
        try {
            for (Map.Entry<String, SerializedObject> e : map.entrySet()) {
                writer.writeEmptyElement(ConfigLoader.SERIALIZED);
                writer.writeAttribute(ConfigLoader.NAME, e.getValue().getName());
                writer.writeAttribute(ConfigLoader.TYPE, e.getValue().getClassName());
                writer.writeAttribute(ConfigLoader.LOCATION, e.getValue().getLocation());
                writer.writeCharacters(System.lineSeparator());
            }
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeStartComponents() throws ConfigWriterException { }

    @Override
    public void writeComponent(Map<String, String> attributes, Map<String, Property> properties) {
        try {
            if (!properties.isEmpty()) {
                writer.writeStartElement(ConfigLoader.COMPONENT);
                writer.writeAttribute(ConfigLoader.NAME, attributes.get(ConfigLoader.NAME));
                writer.writeAttribute(ConfigLoader.TYPE, attributes.get(ConfigLoader.TYPE));
                writer.writeAttribute(ConfigLoader.EXPORT, attributes.get(ConfigLoader.EXPORT));
                writer.writeAttribute(ConfigLoader.IMPORT, attributes.get(ConfigLoader.IMPORT));
                if (attributes.containsKey(ConfigLoader.ENTRIES)) {
                    writer.writeAttribute(ConfigLoader.ENTRIES, attributes.get(ConfigLoader.ENTRIES));
                }
                if (attributes.containsKey(ConfigLoader.LEASETIME)) {
                    writer.writeAttribute(ConfigLoader.LEASETIME, attributes.get(ConfigLoader.LEASETIME));
                }
                if (attributes.containsKey(ConfigLoader.SERIALIZED)) {
                    writer.writeAttribute(ConfigLoader.SERIALIZED, attributes.get(ConfigLoader.SERIALIZED));
                }
                writer.writeCharacters(System.lineSeparator());

                for (Entry<String,Property> property : properties.entrySet()) {
                    String key = property.getKey();
                    Property value = property.getValue();
                    if (value instanceof ListProperty listProp) {
                        //
                        // Must be a string or component list
                        writer.writeCharacters("\t");
                        writer.writeStartElement(ConfigLoader.PROPERTYLIST);
                        writer.writeAttribute(ConfigLoader.NAME, key);
                        writer.writeCharacters(System.lineSeparator());
                        for (SimpleProperty s : listProp.simpleList()) {
                            writer.writeCharacters("\t\t");
                            writer.writeStartElement(ConfigLoader.ITEM);
                            writer.writeCharacters(s.value());
                            writer.writeEndElement();
                            writer.writeCharacters(System.lineSeparator());
                        }
                        for (Class<?> c : listProp.classList()) {
                            writer.writeCharacters("\t\t");
                            writer.writeStartElement(ConfigLoader.TYPE);
                            writer.writeCharacters(c.getName());
                            writer.writeEndElement();
                            writer.writeCharacters(System.lineSeparator());
                        }
                        writer.writeCharacters("\t");
                        writer.writeEndElement();
                        writer.writeCharacters(System.lineSeparator());
                    } else if (value instanceof MapProperty mapProp) {
                        //
                        // Must be a string,string map
                        writer.writeCharacters("\t");
                        writer.writeStartElement(ConfigLoader.PROPERTYMAP);
                        writer.writeAttribute(ConfigLoader.NAME, key);
                        writer.writeCharacters(System.lineSeparator());
                        for (Map.Entry<String, SimpleProperty> e : mapProp.map().entrySet()) {
                            writer.writeCharacters("\t\t");
                            writer.writeEmptyElement(ConfigLoader.ENTRY);
                            writer.writeAttribute(ConfigLoader.KEY, e.getKey());
                            writer.writeAttribute(ConfigLoader.VALUE, e.getValue().value());
                            writer.writeCharacters(System.lineSeparator());
                        }
                        writer.writeCharacters("\t");
                        writer.writeEndElement();
                        writer.writeCharacters(System.lineSeparator());
                    } else {
                        //
                        // Standard property
                        writer.writeCharacters("\t");
                        writer.writeEmptyElement(ConfigLoader.PROPERTY);
                        writer.writeAttribute(ConfigLoader.NAME, key);
                        writer.writeAttribute(ConfigLoader.VALUE, value.toString());
                        writer.writeCharacters(System.lineSeparator());
                    }
                }

                writer.writeEndElement();
            } else {
                writer.writeEmptyElement(ConfigLoader.COMPONENT);
                writer.writeAttribute(ConfigLoader.NAME, attributes.get(ConfigLoader.NAME));
                writer.writeAttribute(ConfigLoader.TYPE, attributes.get(ConfigLoader.TYPE));
                writer.writeAttribute(ConfigLoader.EXPORT, attributes.get(ConfigLoader.EXPORT));
                writer.writeAttribute(ConfigLoader.IMPORT, attributes.get(ConfigLoader.IMPORT));
                if (attributes.containsKey(ConfigLoader.ENTRIES)) {
                    writer.writeAttribute(ConfigLoader.ENTRIES, attributes.get(ConfigLoader.ENTRIES));
                }
                if (attributes.containsKey(ConfigLoader.LEASETIME)) {
                    writer.writeAttribute(ConfigLoader.LEASETIME, attributes.get(ConfigLoader.LEASETIME));
                }
                if (attributes.containsKey(ConfigLoader.SERIALIZED)) {
                    writer.writeAttribute(ConfigLoader.SERIALIZED, attributes.get(ConfigLoader.SERIALIZED));
                }
            }
            writer.writeCharacters(System.lineSeparator());
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeEndComponents() throws ConfigWriterException { }

    @Override
    public void close() throws ConfigWriterException {
        try {
            writer.close();
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

}
