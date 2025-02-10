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

package com.oracle.labs.mlrg.olcut.config.io;

import java.io.InputStream;
import java.net.URL;

/**
 * An interface for configuration loading from on disk formats like xml or json.
 */
public interface ConfigLoader {

    public static final String CONFIG = "config";
    public static final String COMPONENT = "component";
    public static final String PROPERTY = "property";
    public static final String PROPERTYLIST = "propertylist";
    public static final String PROPERTYMAP = "propertymap";
    public static final String NAME = "name";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String SERIALIZED = "serialized";
    public static final String TYPE = "type";
    public static final String ITEM = "item";
    public static final String ENTRY = "entry";
    public static final String FILE = "file";
    public static final String LOCATION = "location";
    public static final String PROPERTIES = "properties";
    public static final String INHERIT = "inherit";
    public static final String EXPORT = "export";
    public static final String IMPORT = "import";
    public static final String ENTRIES = "entries";
    public static final String LEASETIME = "leasetime";
    public static final String GLOBALPROPERTIES = "global-properties";
    public static final String COMPONENTS = "components";
    public static final String SERIALIZEDOBJECTS = "serialized-objects";
    public static final String FILES = "config-files";

    /**
     * Loads configuration from the supplied URL. Will attempt to load it as a
     * classpath resource first.
     * @param url The URL to load from.
     * @throws ConfigLoaderException Thrown if the configuration is malformed or the URL is not resolvable.
     */
    public void load(URL url) throws ConfigLoaderException;

    /**
     * Loads configuration from the stream.
     * <p>
     * Note: does not close the stream.
     * @param stream The stream to load from.
     * @throws ConfigLoaderException Thrown if the configuration is malformed.
     */
    public void load(InputStream stream) throws ConfigLoaderException;

}
