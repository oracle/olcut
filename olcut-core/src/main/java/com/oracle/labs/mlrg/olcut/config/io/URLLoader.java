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

import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Loads in configurations from URLs. Manages the queue of URLs to be processed.
 */
public class URLLoader {

    private final Map<String,ConfigLoader> loaderMap = new HashMap<>();

    private final Queue<URL> urlQueue;

    private final Map<String, FileFormatFactory> formatFactoryMap;

    private final Map<String, ConfigurationData> rpdMap = new HashMap<>();

    private final Map<String, ConfigurationData> existingRPD;

    private final Map<String, SerializedObject> serializedObjects = new HashMap<>();

    private final GlobalProperties globalProperties = new GlobalProperties();

    public URLLoader(Queue<URL> urlQueue, Map<String, FileFormatFactory> formatFactoryMap) {
        this(urlQueue, formatFactoryMap, null);
    }

    public URLLoader(Queue<URL> urlQueue, Map<String, FileFormatFactory> formatFactoryMap, Map<String, ConfigurationData> existingRPD) {
        this.urlQueue = urlQueue;
        this.formatFactoryMap = formatFactoryMap;
        this.existingRPD = existingRPD;
    }

    public void load() throws ConfigLoaderException {
        URL curURL;
        while (!urlQueue.isEmpty()) {
            curURL = urlQueue.poll();
            String filename = curURL.getFile();
            int i = filename.lastIndexOf('.');
            String extension = i > 0 ? filename.substring(i+1).toLowerCase() : "";
            ConfigLoader loader = getLoader(extension);
            loader.load(curURL);
        }
    }

    public void addURL(URL url) {
        urlQueue.add(url);
    }

    public Map<String, ConfigurationData> getPropertyMap() {
        return rpdMap;
    }

    public Map<String, SerializedObject> getSerializedObjects() {
        return serializedObjects;
    }

    public GlobalProperties getGlobalProperties() {
        return globalProperties;
    }

    private ConfigLoader getLoader(String extension) throws ConfigLoaderException {
        ConfigLoader loader = loaderMap.get(extension);
        if (loader == null) {
            FileFormatFactory factory = formatFactoryMap.get(extension);
            if (factory != null) {
                loader = factory.getLoader(this,rpdMap,existingRPD,serializedObjects,globalProperties);
                loaderMap.put(extension,loader);
            } else {
                throw new PropertyException(extension,"Failed to load a handler for '" + extension + "' files.");
            }
        }
        return loader;
    }

}
