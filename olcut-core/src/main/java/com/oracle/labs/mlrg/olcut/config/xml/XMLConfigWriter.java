package com.oracle.labs.mlrg.olcut.config.xml;

import com.oracle.labs.mlrg.olcut.config.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.ListProperty;
import com.oracle.labs.mlrg.olcut.config.MapProperty;
import com.oracle.labs.mlrg.olcut.config.Property;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.SimpleProperty;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 */
public class XMLConfigWriter implements ConfigWriter {
    private final XMLStreamWriter writer;

    public XMLConfigWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    @Override
    public void writeStartDocument() throws ConfigWriterException {
        try {
            writer.writeStartDocument("utf-8", "1.0");
            writer.writeCharacters(System.lineSeparator());
            writer.writeComment("OLCUT configuration file");
            writer.writeCharacters(System.lineSeparator());
            writer.writeStartElement(ConfigLoader.CONFIG);
            writer.writeCharacters(System.lineSeparator());
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeEndDocument() throws ConfigWriterException {
        try {
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeGlobalProperties(Map<String,String> props) throws ConfigWriterException {
        try {
            for (Map.Entry<String, String> e : props.entrySet()) {
                writer.writeEmptyElement(ConfigLoader.PROPERTY);
                writer.writeAttribute(ConfigLoader.NAME, e.getKey());
                writer.writeAttribute(ConfigLoader.VALUE, e.getValue());
                writer.writeCharacters(System.lineSeparator());
            }
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeSerializedObjects(Map<String, SerializedObject> map) throws ConfigWriterException {
        try {
            for (Map.Entry<String, SerializedObject> e : map.entrySet()) {
                writer.writeEmptyElement(ConfigLoader.SERIALIZED);
                writer.writeAttribute(ConfigLoader.NAME, e.getValue().getName());
                writer.writeAttribute(ConfigLoader.TYPE, e.getValue().getClassName());
                writer.writeAttribute(ConfigLoader.LOCATION, e.getValue().getLocation());
                writer.writeCharacters(System.lineSeparator());
            }
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeStartComponents() throws ConfigWriterException { }

    @Override
    public void writeComponent(Map<String, String> attributes, Map<String, Property> properties) {
        try {
            if (!properties.isEmpty()) {
                writer.writeStartElement(ConfigLoader.COMPONENT);
                writer.writeAttribute(ConfigLoader.NAME, attributes.get(ConfigLoader.NAME));
                writer.writeAttribute(ConfigLoader.TYPE, attributes.get(ConfigLoader.TYPE));
                writer.writeAttribute(ConfigLoader.EXPORT, attributes.get(ConfigLoader.EXPORT));
                writer.writeAttribute(ConfigLoader.IMPORT, attributes.get(ConfigLoader.IMPORT));
                if (attributes.containsKey(ConfigLoader.ENTRIES)) {
                    writer.writeAttribute(ConfigLoader.ENTRIES, attributes.get(ConfigLoader.ENTRIES));
                }
                if (attributes.containsKey(ConfigLoader.LEASETIME)) {
                    writer.writeAttribute(ConfigLoader.LEASETIME, attributes.get(ConfigLoader.LEASETIME));
                }
                if (attributes.containsKey(ConfigLoader.SERIALIZED)) {
                    writer.writeAttribute(ConfigLoader.SERIALIZED, attributes.get(ConfigLoader.SERIALIZED));
                }
                writer.writeCharacters(System.lineSeparator());

                for (Entry<String,Property> property : properties.entrySet()) {
                    String key = property.getKey();
                    Property value = property.getValue();
                    if (value instanceof ListProperty) {
                        //
                        // Must be a string or component list
                        writer.writeCharacters("\t");
                        writer.writeStartElement(ConfigLoader.PROPERTYLIST);
                        writer.writeAttribute(ConfigLoader.NAME, key);
                        writer.writeCharacters(System.lineSeparator());
                        for (SimpleProperty s : ((ListProperty) value).getSimpleList()) {
                            writer.writeCharacters("\t\t");
                            writer.writeStartElement(ConfigLoader.ITEM);
                            writer.writeCharacters(s.getValue());
                            writer.writeEndElement();
                            writer.writeCharacters(System.lineSeparator());
                        }
                        for (Class<?> c : ((ListProperty)value).getClassList()) {
                            writer.writeCharacters("\t\t");
                            writer.writeStartElement(ConfigLoader.TYPE);
                            writer.writeCharacters(c.getName());
                            writer.writeEndElement();
                            writer.writeCharacters(System.lineSeparator());
                        }
                        writer.writeCharacters("\t");
                        writer.writeEndElement();
                        writer.writeCharacters(System.lineSeparator());
                    } else if (value instanceof MapProperty) {
                        //
                        // Must be a string,string map
                        writer.writeCharacters("\t");
                        writer.writeStartElement(ConfigLoader.PROPERTYMAP);
                        writer.writeAttribute(ConfigLoader.NAME, key);
                        writer.writeCharacters(System.lineSeparator());
                        for (Map.Entry<String, SimpleProperty> e : ((MapProperty) value).getMap().entrySet()) {
                            writer.writeCharacters("\t\t");
                            writer.writeEmptyElement(ConfigLoader.ENTRY);
                            writer.writeAttribute(ConfigLoader.KEY, e.getKey());
                            writer.writeAttribute(ConfigLoader.VALUE, e.getValue().getValue());
                            writer.writeCharacters(System.lineSeparator());
                        }
                        writer.writeCharacters("\t");
                        writer.writeEndElement();
                        writer.writeCharacters(System.lineSeparator());
                    } else {
                        //
                        // Standard property
                        writer.writeCharacters("\t");
                        writer.writeEmptyElement(ConfigLoader.PROPERTY);
                        writer.writeAttribute(ConfigLoader.NAME, key);
                        writer.writeAttribute(ConfigLoader.VALUE, value.toString());
                        writer.writeCharacters(System.lineSeparator());
                    }
                }

                writer.writeEndElement();
            } else {
                writer.writeEmptyElement(ConfigLoader.COMPONENT);
                writer.writeAttribute(ConfigLoader.NAME, attributes.get(ConfigLoader.NAME));
                writer.writeAttribute(ConfigLoader.TYPE, attributes.get(ConfigLoader.TYPE));
                writer.writeAttribute(ConfigLoader.EXPORT, attributes.get(ConfigLoader.EXPORT));
                writer.writeAttribute(ConfigLoader.IMPORT, attributes.get(ConfigLoader.IMPORT));
                if (attributes.containsKey(ConfigLoader.ENTRIES)) {
                    writer.writeAttribute(ConfigLoader.ENTRIES, attributes.get(ConfigLoader.ENTRIES));
                }
                if (attributes.containsKey(ConfigLoader.LEASETIME)) {
                    writer.writeAttribute(ConfigLoader.LEASETIME, attributes.get(ConfigLoader.LEASETIME));
                }
                if (attributes.containsKey(ConfigLoader.SERIALIZED)) {
                    writer.writeAttribute(ConfigLoader.SERIALIZED, attributes.get(ConfigLoader.SERIALIZED));
                }
            }
            writer.writeCharacters(System.lineSeparator());
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeEndComponents() throws ConfigWriterException { }

    @Override
    public void close() throws ConfigWriterException {
        try {
            writer.close();
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

}
