package com.oracle.labs.mlrg.olcut.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.oracle.labs.mlrg.olcut.config.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 */
public class JsonConfigWriter implements ConfigWriter {

    private JsonGenerator writer;

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
            writer.writeObjectFieldStart(ConfigLoader.GLOBALPROPERTIES);
            for (Entry<String,String> e : props.entrySet()) {
                writer.writeStringField(e.getKey(),e.getValue());
            }
            writer.writeEndObject();
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeSerializedObjects(Map<String, SerializedObject> map) throws ConfigWriterException {
        try {
            writer.writeArrayFieldStart(ConfigLoader.SERIALIZEDOBJECTS);
            for (Entry<String,SerializedObject> e : map.entrySet()) {
                writer.writeStartObject();
                writer.writeStringField(ConfigLoader.NAME,e.getValue().getName());
                writer.writeStringField(ConfigLoader.TYPE,e.getValue().getClassName());
                writer.writeStringField(ConfigLoader.LOCATION,e.getValue().getLocation());
                writer.writeEndObject();
            }
            writer.writeEndArray();
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
    public void writeComponent(Map<String, String> attributes, Map<String, Object> properties) {
        try {
            writer.writeStartObject();
            writer.writeStringField(ConfigLoader.NAME, attributes.get(ConfigLoader.NAME));
            writer.writeStringField(ConfigLoader.TYPE, attributes.get(ConfigLoader.TYPE));
            writer.writeStringField(ConfigLoader.EXPORT, attributes.get(ConfigLoader.EXPORT));
            writer.writeStringField(ConfigLoader.IMPORT, attributes.get(ConfigLoader.IMPORT));
            if (attributes.containsKey(ConfigLoader.ENTRIES)) {
                writer.writeStringField(ConfigLoader.ENTRIES, attributes.get(ConfigLoader.ENTRIES));
            }
            if (attributes.containsKey(ConfigLoader.LEASETIME)) {
                writer.writeStringField(ConfigLoader.LEASETIME, attributes.get(ConfigLoader.LEASETIME));
            }
            if (attributes.containsKey(ConfigLoader.SERIALIZED)) {
                writer.writeStringField(ConfigLoader.SERIALIZED, attributes.get(ConfigLoader.SERIALIZED));
            }

            if (!properties.isEmpty()) {
                writer.writeObjectFieldStart(ConfigLoader.PROPERTIES);
                for (Entry<String, Object> property : properties.entrySet()) {
                    String key = property.getKey();
                    Object value = property.getValue();
                    if (value instanceof List) {
                        //
                        // Must be a string or component list
                        writer.writeArrayFieldStart(key);
                        for (Object o : (List) value) {
                            writer.writeStartObject();
                            if (o instanceof Class) {
                                writer.writeStringField(ConfigLoader.TYPE,((Class)o).getName());
                            } else {
                                writer.writeStringField(ConfigLoader.ITEM,o.toString());
                            }
                            writer.writeEndObject();
                        }
                        writer.writeEndArray();
                    } else if (value instanceof Map) {
                        //
                        // Must be a string,string map
                        writer.writeObjectFieldStart(key);
                        for (Map.Entry<String, String> e : ((Map<String, String>) value).entrySet()) {
                            writer.writeStringField(e.getKey(),e.getValue());
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
