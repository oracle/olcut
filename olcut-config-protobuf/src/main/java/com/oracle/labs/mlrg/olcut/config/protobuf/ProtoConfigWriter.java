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

package com.oracle.labs.mlrg.olcut.config.protobuf;

import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.Property;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.ComponentProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.ConfigProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.PropertyListProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.PropertyMapProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.SerializedObjectProto;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * A {@link ConfigWriter} for protobuf format config files.
 */
public final class ProtoConfigWriter implements ConfigWriter {

    private final OutputStream writer;
    private final boolean writeAsText;

    private final ConfigProto.Builder builder;

    /**
     * Constructs a writer for a protobuf config file.
     *
     * @param writer      The writer to write to.
     * @param writeAsText Write as text instead of binary.
     */
    public ProtoConfigWriter(OutputStream writer, boolean writeAsText) {
        this.writer = writer;
        this.writeAsText = writeAsText;
        this.builder = ConfigProto.newBuilder();
    }

    @Override
    public void writeStartDocument() throws ConfigWriterException {
        // No-op
    }

    @Override
    public void writeEndDocument() throws ConfigWriterException {
        // No-op
    }

    @Override
    public void writeGlobalProperties(Map<String, String> props) throws ConfigWriterException {
        builder.putAllProperties(props);
    }

    @Override
    public void writeSerializedObjects(Map<String, SerializedObject> map) throws ConfigWriterException {
        for (Map.Entry<String, SerializedObject> e : map.entrySet()) {
            SerializedObject<?> serObj = e.getValue();
            SerializedObjectProto.Builder serObjBuilder = SerializedObjectProto.newBuilder();
            serObjBuilder.setName(serObj.getName());
            serObjBuilder.setType(serObj.getClassName());
            serObjBuilder.setLocation(serObj.getLocation());
            builder.addSerializedObject(serObjBuilder.build());
        }
    }

    @Override
    public void writeStartComponents() throws ConfigWriterException {
        // No-op
    }

    @Override
    public void writeComponent(Map<String, String> attributes, Map<String, Property> properties) {
        ComponentProto.Builder componentBuilder = ComponentProto.newBuilder();
        componentBuilder.setName(attributes.get(ConfigLoader.NAME));
        componentBuilder.setType(attributes.get(ConfigLoader.TYPE));
        if (attributes.get(ConfigLoader.EXPORT).equalsIgnoreCase("true")) {
            componentBuilder.setExportable(true);
        }
        if (attributes.get(ConfigLoader.IMPORT).equalsIgnoreCase("true")) {
            componentBuilder.setImportable(true);
        }
        if (attributes.containsKey(ConfigLoader.ENTRIES)) {
            componentBuilder.setEntries(attributes.get(ConfigLoader.ENTRIES));
        }
        if (attributes.containsKey(ConfigLoader.LEASETIME)) {
            componentBuilder.setLeaseTime(Long.parseLong(attributes.get(ConfigLoader.LEASETIME)));
        }
        if (attributes.containsKey(ConfigLoader.SERIALIZED)) {
            componentBuilder.setSerialized(attributes.get(ConfigLoader.SERIALIZED));
        }

        for (Map.Entry<String, Property> property : properties.entrySet()) {
            String key = property.getKey();
            Property value = property.getValue();
            if (value instanceof ListProperty listProp) {
                //
                // Must be a string/component list
                PropertyListProto.Builder listBuilder = PropertyListProto.newBuilder();
                listBuilder.setName(key);
                for (SimpleProperty s : listProp.simpleList()) {
                    listBuilder.addItem(s.value());
                }
                for (Class<?> c : listProp.classList()) {
                    listBuilder.addType(c.getName());
                }
                componentBuilder.addListProperty(listBuilder.build());
            } else if (value instanceof MapProperty mapProp) {
                //
                // Must be a string,string map
                PropertyMapProto.Builder mapBuilder = PropertyMapProto.newBuilder();
                mapBuilder.setName(key);
                for (Map.Entry<String, SimpleProperty> e : mapProp.map().entrySet()) {
                    mapBuilder.putElements(e.getKey(), e.getValue().value());
                }
                componentBuilder.addMapProperty(mapBuilder.build());
            } else {
                //
                // Standard property
                componentBuilder.putProperties(key, value.toString());
            }
        }
        builder.addComponents(componentBuilder.build());
    }

    @Override
    public void writeEndComponents() throws ConfigWriterException {
        // No-op
    }

    @Override
    public void close() throws ConfigWriterException {
        ConfigProto proto = builder.build();
        try {
            if (writeAsText) {
                PrintStream stream = new PrintStream(writer);
                stream.println(proto.toString());
                stream.close();
            } else {
                proto.writeTo(writer);
                writer.close();
            }
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }
}
