package com.oracle.labs.mlrg.olcut.config.xml;

import com.oracle.labs.mlrg.olcut.config.io.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.io.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.io.FileFormatFactory;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.io.URLLoader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;

import java.io.OutputStream;
import java.util.Map;

/**
 *
 */
public class XMLConfigFactory implements FileFormatFactory {

    private final XMLOutputFactory factory = XMLOutputFactory.newFactory();

    public XMLConfigFactory() {

    }

    @Override
    public String getExtension() {
        return "xml";
    }

    @Override
    public ConfigLoader getLoader(URLLoader parent, Map<String, ConfigurationData> rpdMap, Map<String, ConfigurationData> existingRPD, Map<String, SerializedObject> serializedObjects, GlobalProperties globalProperties) throws ConfigLoaderException {
        try {
            return new SAXLoader(parent, rpdMap, existingRPD, serializedObjects, globalProperties);
        } catch (SAXException | ParserConfigurationException e) {
            throw new ConfigLoaderException(e);
        }
    }

    @Override
    public ConfigWriter getWriter(OutputStream writer) throws ConfigWriterException {
        try {
            XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer, "utf-8");
            return new XMLConfigWriter(xmlWriter);
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }
}
