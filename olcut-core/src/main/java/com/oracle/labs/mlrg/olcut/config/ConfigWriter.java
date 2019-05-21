package com.oracle.labs.mlrg.olcut.config;

import java.util.Map;

/**
 *
 */
public interface ConfigWriter {
    public void writeStartDocument() throws ConfigWriterException;
    public void writeEndDocument() throws ConfigWriterException;

    public void writeGlobalProperties(Map<String,String> props) throws ConfigWriterException;

    public void writeSerializedObjects(Map<String, SerializedObject> map) throws ConfigWriterException;

    public void writeStartComponents() throws ConfigWriterException;
    public void writeComponent(Map<String,String> attributes, Map<String,Property> properties);
    public void writeEndComponents() throws ConfigWriterException;

    public void close() throws ConfigWriterException;
}
