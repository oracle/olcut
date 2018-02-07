package com.oracle.labs.mlrg.olcut.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.oracle.labs.mlrg.olcut.config.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.ConfigWriterException;

import java.io.IOException;
import java.util.Map;

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
            writer.writeArrayFieldStart("config");
            writer.writeRaw(System.lineSeparator());
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeStartElement(String name) throws ConfigWriterException {
        try {
            writer.writeStartObject();
            writer.writeStringField(JsonLoader.CONFIG_TYPE, name);
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeRaw(String input) throws ConfigWriterException {
        try {
            writer.writeRaw(input);
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeMember(String input) throws ConfigWriterException {
        try {
            writer.writeStringField(JsonLoader.MEMBER,input);
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeAttribute(String name, String value) throws ConfigWriterException {
        try {
            writer.writeStringField(name,value);
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeEndElement() throws ConfigWriterException {
        try {
            writer.writeEndObject();
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeElement(String name, Map<String, String> attributes) throws ConfigWriterException {
        try {
            writer.writeStartObject();
            writer.writeStringField(JsonLoader.CONFIG_TYPE,name);
            for (Map.Entry<String,String> e : attributes.entrySet()) {
                writer.writeStringField(e.getKey(),e.getValue());
            }
            writer.writeEndObject();
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeArrayStart(String name) throws ConfigWriterException {
        try {
            writer.writeArrayFieldStart(name);
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeArrayEnd() throws ConfigWriterException {
        try {
            writer.writeEndArray();
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeEndDocument() throws ConfigWriterException {
        try {
            writer.writeEndArray();
            writer.writeEndObject();
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
