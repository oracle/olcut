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
