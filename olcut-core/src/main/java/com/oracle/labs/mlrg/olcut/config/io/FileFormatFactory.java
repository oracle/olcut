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

import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;

import java.io.OutputStream;
import java.util.Map;

/**
 * An interface which generates loaders and writers for a specific file format.
 */
public interface FileFormatFactory {

    /**
     * The file extension this factory supports.
     * @return The file extension.
     */
    public String getExtension();

    /**
     * Constructs a loader for this file format.
     * @param parent The parent loader for discovering parent components.
     * @param rpdMap The current property map.
     * @param existingRPD Any existing configurations.
     * @param serializedObjects Any serialized objects.
     * @param globalProperties The global property map.
     * @return A loader for this file format.
     * @throws ConfigLoaderException If the loader could not be instantiated.
     */
    public ConfigLoader getLoader(URLLoader parent,
                                  Map<String, ConfigurationData> rpdMap,
                                  Map<String, ConfigurationData> existingRPD,
                                  Map<String, SerializedObject> serializedObjects,
                                  GlobalProperties globalProperties) throws ConfigLoaderException;

    /**
     * Constructs a writer for this file format.
     * @param os The output stream to write to.
     * @return A writer for this file format.
     * @throws ConfigWriterException If the writer could not be instantiated.
     */
    public ConfigWriter getWriter(OutputStream os) throws ConfigWriterException;

}
