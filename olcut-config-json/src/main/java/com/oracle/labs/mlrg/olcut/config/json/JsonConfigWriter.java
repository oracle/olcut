/*
 * Copyright (c) 2018, 2025, Oracle and/or its affiliates.
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.Property;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link ConfigWriter} for json format config files.
 */
public final class JsonConfigWriter implements ConfigWriter {

    private final JsonGenerator writer;

    /**
     * Constructs a JsonConfigWriter from the supplied JsonGenerator.
     * @param writer The json generator.
     */
    public JsonConfigWriter(JsonGenerator writer) {
        this.writer = writer;
    }

    @Override
    public void writeStartDocument() throws ConfigWriterException {
        try {
            writer.writeStartObject();
            writer.writeObjectFieldStart(ConfigLoader.CONFIG);
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeEndDocument() throws ConfigWriterException {
        try {
            writer.writeEndObject();
            writer.writeEndObject();
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeGlobalProperties(Map<String, String> props) throws ConfigWriterException {
        try {
            if (!props.isEmpty()) {
                writer.writeObjectFieldStart(ConfigLoader.GLOBALPROPERTIES);
                for (Entry<String, String> e : props.entrySet()) {
                    writer.writeStringField(e.getKey(), e.getValue());
                }
                writer.writeEndObject();
            }
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeSerializedObjects(Map<String, SerializedObject> map) throws ConfigWriterException {
        try {
            if (!map.isEmpty()) {
                writer.writeArrayFieldStart(ConfigLoader.SERIALIZEDOBJECTS);
                for (Entry<String, SerializedObject> e : map.entrySet()) {
                    writer.writeStartObject();
                    writer.writeStringField(ConfigLoader.NAME, e.getValue().getName());
                    writer.writeStringField(ConfigLoader.TYPE, e.getValue().getClassName());
                    writer.writeStringField(ConfigLoader.LOCATION, e.getValue().getLocation());
                    writer.writeEndObject();
                }
                writer.writeEndArray();
            }
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeStartComponents() throws ConfigWriterException {
        try {
            writer.writeArrayFieldStart(ConfigLoader.COMPONENTS);
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeComponent(Map<String, String> attributes, Map<String, Property> properties) {
        try {
            writer.writeStartObject();
            writer.writeStringField(ConfigLoader.NAME, attributes.get(ConfigLoader.NAME));
            writer.writeStringField(ConfigLoader.TYPE, attributes.get(ConfigLoader.TYPE));
            if (attributes.containsKey(ConfigLoader.SERIALIZED)) {
                writer.writeStringField(ConfigLoader.SERIALIZED, attributes.get(ConfigLoader.SERIALIZED));
            }

            if (!properties.isEmpty()) {
                writer.writeObjectFieldStart(ConfigLoader.PROPERTIES);
                for (Entry<String, Property> property : properties.entrySet()) {
                    String key = property.getKey();
                    Property value = property.getValue();
                    if (value instanceof ListProperty) {
                        //
                        // Must be a string or component list
                        writer.writeArrayFieldStart(key);
                        for (SimpleProperty s : ((ListProperty) value).getSimpleList()) {
                            writer.writeStartObject();
                            writer.writeStringField(ConfigLoader.ITEM,s.getValue());
                            writer.writeEndObject();
                        }
                        for (Class<?> c : ((ListProperty) value).getClassList()) {
                            writer.writeStartObject();
                            writer.writeStringField(ConfigLoader.TYPE,c.getName());
                            writer.writeEndObject();
                        }
                        writer.writeEndArray();
                    } else if (value instanceof MapProperty) {
                        //
                        // Must be a string,string map
                        writer.writeObjectFieldStart(key);
                        for (Map.Entry<String, SimpleProperty> e : ((MapProperty) value).getMap().entrySet()) {
                            writer.writeStringField(e.getKey(),e.getValue().getValue());
                        }
                        writer.writeEndObject();
                    } else {
                        //
                        // Standard property
                        writer.writeStringField(key,value.toString());
                    }
                }
                writer.writeEndObject();
            }

            writer.writeEndObject();
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeEndComponents() throws ConfigWriterException {
        try {
            writer.writeEndArray();
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void close() throws ConfigWriterException {
        try {
            writer.close();
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }
}
