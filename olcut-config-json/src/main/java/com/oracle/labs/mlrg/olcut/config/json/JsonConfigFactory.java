package com.oracle.labs.mlrg.olcut.config.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.io.FileFormatFactory;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.io.URLLoader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 */
public class JsonConfigFactory implements FileFormatFactory {

    private final JsonFactory factory = new JsonFactory();

    @Override
    public String getExtension() {
        return "json";
    }

    @Override
    public ConfigLoader getLoader(URLLoader parent, Map<String, ConfigurationData> rpdMap, Map<String, ConfigurationData> existingRPD, Map<String, SerializedObject> serializedObjects, GlobalProperties globalProperties) throws ConfigLoaderException {
        return new JsonLoader(factory,parent,rpdMap,existingRPD,serializedObjects,globalProperties);
    }

    @Override
    public ConfigWriter getWriter(OutputStream writer) throws ConfigWriterException {
        try {
            JsonGenerator jsonWriter = factory.createGenerator(writer);
            jsonWriter.setPrettyPrinter(new DefaultPrettyPrinter());
            return new JsonConfigWriter(jsonWriter);
        } catch (IOException e) {
            throw new ConfigWriterException(e);
        }
    }
}
