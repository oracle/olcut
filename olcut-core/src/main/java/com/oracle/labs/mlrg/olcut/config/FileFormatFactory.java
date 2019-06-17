package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;

import java.io.OutputStream;
import java.util.Map;

/**
 * An interface which generates loaders and writers for a specific file format.
 */
public interface FileFormatFactory {

    public String getExtension();

    public ConfigLoader getLoader(URLLoader parent,
                                  Map<String, ConfigurationData> rpdMap,
                                  Map<String, ConfigurationData> existingRPD,
                                  Map<String, SerializedObject> serializedObjects,
                                  GlobalProperties globalProperties) throws ConfigLoaderException;

    public ConfigWriter getWriter(OutputStream os) throws ConfigWriterException;

}
