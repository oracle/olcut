package com.oracle.labs.mlrg.olcut.config.xml;

import com.oracle.labs.mlrg.olcut.config.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.ConfigWriterException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.util.Map;

/**
 *
 */
public class XMLConfigWriter implements ConfigWriter {
    private XMLStreamWriter writer;

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
            writer.writeStartElement("config");
            writer.writeCharacters(System.lineSeparator());
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeStartElement(String name) throws ConfigWriterException {
        try {
            writer.writeStartElement(name);
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeRaw(String input) throws ConfigWriterException {
        try {
            writer.writeCharacters(input);
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeMember(String input) throws ConfigWriterException {
        try {
            writer.writeCharacters(input);
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeAttribute(String name, String value) throws ConfigWriterException {
        try {
            writer.writeAttribute(name,value);
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeEndElement() throws ConfigWriterException {
        try {
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeElement(String name, Map<String, String> attributes) throws ConfigWriterException {
        try {
            writer.writeEmptyElement(name);
            for (Map.Entry<String,String> e : attributes.entrySet()) {
                writer.writeAttribute(e.getKey(),e.getValue());
            }
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeArrayStart(String name) throws ConfigWriterException { }

    @Override
    public void writeArrayEnd() throws ConfigWriterException { }

    @Override
    public void writeEndDocument() throws ConfigWriterException {
        try {
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void close() throws ConfigWriterException {
        try {
            writer.close();
        } catch (XMLStreamException e) {
            throw new ConfigWriterException(e);
        }
    }

}
