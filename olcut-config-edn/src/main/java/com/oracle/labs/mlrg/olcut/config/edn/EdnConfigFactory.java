package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.FileFormatFactory;
import com.oracle.labs.mlrg.olcut.config.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.RawPropertyData;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.URLLoader;

import java.io.OutputStream;
import java.util.Map;

public class EdnConfigFactory implements FileFormatFactory {

    @Override
    public String getExtension() {
        return "edn";
    }

    @Override
    public ConfigLoader getLoader(URLLoader parent, Map<String, RawPropertyData> rpdMap, Map<String, RawPropertyData> existingRPD, Map<String, SerializedObject> serializedObjects, GlobalProperties globalProperties) throws ConfigLoaderException {
        return new EdnLoader(parent, rpdMap, existingRPD, serializedObjects, globalProperties);
    }

    @Override
    public ConfigWriter getWriter(OutputStream os) throws ConfigWriterException {
        return new EdnConfigWriter(os);
    }
}
