package com.oracle.labs.mlrg.olcut.config.io;

import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.property.Property;

import java.util.Map;

/**
 * An interface for writing out configuration in formats like xml or json.
 */
public interface ConfigWriter {
    /**
     * Writes the document preamble.
     * @throws ConfigWriterException Thrown if the stream is not writable.
     */
    public void writeStartDocument() throws ConfigWriterException;

    /**
     * Writes the document closing syntax, if any.
     * @throws ConfigWriterException Thrown if the stream is not writeable.
     */
    public void writeEndDocument() throws ConfigWriterException;

    /**
     * Writes out the global properties.
     * @param props The global properties.
     * @throws ConfigWriterException Thrown if the stream is not writeable.
     */
    public void writeGlobalProperties(Map<String,String> props) throws ConfigWriterException;

    /**
     * Writes out the map of serialized objects.
     * @param map The serialized objects.
     * @throws ConfigWriterException Thrown if the stream is not writeable.
     */
    public void writeSerializedObjects(Map<String, SerializedObject> map) throws ConfigWriterException;

    /**
     * Writes the start of the configurable list.
     * @throws ConfigWriterException Thrown if the stream is not writeable.
     */
    public void writeStartComponents() throws ConfigWriterException;

    /**
     * Writes out configuration for a single object.
     * @param attributes The attributes for this object.
     * @param properties The properties for this object.
     */
    public void writeComponent(Map<String,String> attributes, Map<String, Property> properties);

    /**
     * Writes the end of the configurable list.
     * @throws ConfigWriterException Thrown if the stream is not writeable.
     */
    public void writeEndComponents() throws ConfigWriterException;

    /**
     * Closes the writer, but not the underlying stream.
     * @throws ConfigWriterException Thrown if the stream is not writeable.
     */
    public void close() throws ConfigWriterException;
}
