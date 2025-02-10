/*
 * Copyright (c) 2004, 2025, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.util.IOUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * A class to hold the information for a serialized Object that is defined in a
 * configuration file.
 */
public final class SerializedObject<T> {

    private ConfigurationManager configurationManager;

    private final String name;

    private final String location;

    private final String className;

    private T object;

    /**
     * The configuration for a serialized object.
     * @param name The name of the object in the config file.
     * @param location The location of the serialized file.
     * @param className The class name of the object.
     */
    public SerializedObject(String name, String location, String className) {
        this.name = name;
        this.location = location;
        this.className = className;
    }

    /**
     * Sets the configuration manager that hosts this serialized object.
     * @param configurationManager The host configuration manager.
     */
    void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    /**
     * The name of the serialized object.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * The location of the serialized object (either a path on disk or a classpath URL).
     * @return The location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * The class name of the serialized object.
     * @return The class name.
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * Gets the deserialized object that we represent.
     * @return the object
     * @throws PropertyException if the object cannot be deserialized.
     */
    @SuppressWarnings("unchecked")// throws PropertyException if the serialised type doesn't match the class name.
    public T getObject() throws PropertyException {
        if (object == null) {
            String actualLocation = configurationManager.getImmutableGlobalProperties().replaceGlobalProperties(name, null, location);
            InputStream serStream = IOUtil.getInputStreamForLocation(actualLocation);
            try {
                Class<T> objectClass = (Class<T>) Class.forName(className);
                if (serStream != null) {
                    try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(serStream, 1024 * 1024))) {
                        //
                        // Read the object and cast it into this class for return;
                        object = objectClass.cast(ois.readObject());
                    } catch (ClassCastException ex) {
                        throw new PropertyException(ex, name, "Failed to cast object to type " + objectClass.getName());
                    } catch (IOException ex) {
                        throw new PropertyException(ex, name, "Error reading serialized form from " + actualLocation);
                    }
                } else {
                    throw new PropertyException(name, "Failed to open stream from location " + actualLocation);
                }
            } catch (ClassNotFoundException ex) {
                throw new PropertyException(ex, name, "Serialized class " + className + " not found for " + actualLocation);
            }
        }
        return object;
    }

}
