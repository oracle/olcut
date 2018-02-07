package com.oracle.labs.mlrg.olcut.config;

import java.util.Map;

/**
 *
 */
public interface ConfigWriter {
    public void writeStartDocument() throws ConfigWriterException;

    public void writeStartElement(String name) throws ConfigWriterException;

    public void writeRaw(String input) throws ConfigWriterException;

    public void writeMember(String input) throws ConfigWriterException;

    public void writeAttribute(String name, String value) throws ConfigWriterException;

    public void writeEndElement() throws ConfigWriterException;

    public void writeElement(String name, Map<String,String> attributes) throws ConfigWriterException;

    public void writeArrayStart(String name) throws ConfigWriterException;

    public void writeArrayEnd() throws ConfigWriterException;

    public void writeEndDocument() throws ConfigWriterException;

    public void close() throws ConfigWriterException;
}
