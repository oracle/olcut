package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.io.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.io.FileFormatFactory;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.io.URLLoader;

import java.io.OutputStream;
import java.util.Map;

public class EdnConfigFactory implements FileFormatFactory {

    @Override
    public String getExtension() {
        return "edn";
    }

    @Override
    public ConfigLoader getLoader(URLLoader parent, Map<String, ConfigurationData> rpdMap, Map<String, ConfigurationData> existingRPD, Map<String, SerializedObject> serializedObjects, GlobalProperties globalProperties) throws ConfigLoaderException {
        return new EdnLoader(parent, rpdMap, existingRPD, serializedObjects, globalProperties);
    }

    @Override
    public ConfigWriter getWriter(OutputStream os) throws ConfigWriterException {
        return new EdnConfigWriter(os);
    }
}
