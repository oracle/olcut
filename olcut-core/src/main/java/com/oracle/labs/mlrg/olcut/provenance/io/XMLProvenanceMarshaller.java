/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.provenance.io;

import com.oracle.labs.mlrg.olcut.util.Pair;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Class for serializing and deserializing provenances to/from xml.
 */
public final class XMLProvenanceMarshaller implements ProvenanceMarshaller {

    /**
     * Container for all provenances.
     */
    public static final String PROVENANCES = "provenances";
    /**
     * {@link ObjectMarshalledProvenance} element.
     */
    public static final String OBJECT_MARSHALLED_PROVENANCE = "object-provenance";
    /**
     * {@link ListMarshalledProvenance} element.
     */
    public static final String LIST_MARSHALLED_PROVENANCE = "list-provenance";
    /**
     * {@link MapMarshalledProvenance} element.
     */
    public static final String MAP_MARSHALLED_PROVENANCE = "map-provenance";
    /**
     * {@link SimpleMarshalledProvenance} element.
     */
    public static final String SIMPLE_MARSHALLED_PROVENANCE = "simple-provenance";
    /**
     * Object name attribute.
     */
    public static final String OBJECT_NAME = "obj-name";
    /**
     * Object class name attribute.
     */
    public static final String OBJECT_CLASS_NAME = "class-name";
    /**
     * Provenance class name attribute.
     */
    public static final String PROVENANCE_CLASS_NAME = "prov-class-name";
    /**
     * Provenance key attribute.
     */
    public static final String PROV_KEY = "key";
    /**
     * {@link SimpleMarshalledProvenance} value attribute.
     */
    public static final String PROV_VALUE = "value";
    /**
     * {@link SimpleMarshalledProvenance} additional value attribute.
     */
    public static final String PROV_ADDITIONAL = "additional";
    /**
     * {@link SimpleMarshalledProvenance} is reference atttribute.
     */
    public static final String IS_REFERENCE = "is-reference";

    private final XMLOutputFactory factory = XMLOutputFactory.newFactory();

    private final boolean prettyPrint;

    /**
     * Constructs an XMLProvenanceMarshaller.
     *
     * @param prettyPrint Print tabs and newlines to appropriately format the XML output for readability.
     */
    public XMLProvenanceMarshaller(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    @Override
    public String getFileExtension() {
        return "xml";
    }

    @Override
    public List<ObjectMarshalledProvenance> deserializeFromFile(Path path) throws IOException {
        return null;
    }

    @Override
    public List<ObjectMarshalledProvenance> deserializeFromString(String input) {
        return null;
    }

    @Override
    public String serializeToString(List<ObjectMarshalledProvenance> marshalledProvenances) {
        try {
            StringWriter strWriter = new StringWriter();
            XMLStreamWriter writer = factory.createXMLStreamWriter(strWriter);
            writeProvenance(writer, marshalledProvenances);
            return strWriter.toString();
        } catch (XMLStreamException e) {
            throw new IllegalArgumentException("Failed to serialize to XML", e);
        }
    }

    @Override
    public void serializeToFile(List<ObjectMarshalledProvenance> marshalledProvenances, Path path) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(path))) {
            XMLStreamWriter writer = factory.createXMLStreamWriter(bos, "utf-8");
            writeProvenance(writer, marshalledProvenances);
        } catch (XMLStreamException e) {
            throw new IllegalArgumentException("Failed to serialize to XML", e);
        }
    }

    /**
     * Writes the provenance to the XML stream.
     *
     * @param writer                The XML writer.
     * @param marshalledProvenances The provenances.
     */
    private void writeProvenance(XMLStreamWriter writer, List<ObjectMarshalledProvenance> marshalledProvenances) throws XMLStreamException {
        // write preamble
        writer.writeStartDocument("utf-8", "1.0");
        if (prettyPrint) {
            writer.writeCharacters(System.lineSeparator());
        }
        writer.writeStartElement(PROVENANCES);
        if (prettyPrint) {
            writer.writeCharacters(System.lineSeparator());
        }

        // write the provenances
        for (ObjectMarshalledProvenance omp : marshalledProvenances) {
            writeOMP(writer, omp);
        }

        // write end document, closing all tags
        writer.writeEndDocument();
    }

    /**
     * Writes the supplied ObjectMarshalledProvenance to the XML stream.
     *
     * @param writer The XML writer.
     * @param omp    The provenance.
     * @throws XMLStreamException If the XML is invalid.
     */
    private void writeOMP(XMLStreamWriter writer, ObjectMarshalledProvenance omp) throws XMLStreamException {
        Map<String, FlatMarshalledProvenance> provMap = omp.getMap();
        if (prettyPrint) {
            writer.writeCharacters("\t");
        }
        if (!provMap.isEmpty()) {
            writer.writeStartElement(OBJECT_MARSHALLED_PROVENANCE);
        } else {
            writer.writeEmptyElement(OBJECT_MARSHALLED_PROVENANCE);
        }
        writer.writeAttribute(OBJECT_NAME, omp.getName());
        writer.writeAttribute(OBJECT_CLASS_NAME, omp.getObjectClassName());
        writer.writeAttribute(PROVENANCE_CLASS_NAME, omp.getProvenanceClassName());
        if (!provMap.isEmpty()) {
            if (prettyPrint) {
                writer.writeCharacters(System.lineSeparator());
            }
            for (Map.Entry<String,FlatMarshalledProvenance> e : provMap.entrySet()) {
                dispatchFMP(writer,e.getKey(),e.getValue(),2);
            }
            if (prettyPrint) {
                writer.writeCharacters("\t");
            }
            writer.writeEndElement();
        }
        if (prettyPrint) {
            writer.writeCharacters(System.lineSeparator());
        }
    }

    /**
     * Writes the supplied SimpleMarshalledProvenance to the XML stream.
     * @param writer The XML writer.
     * @param smp The provenance to write.
     * @param depth The depth for tabbing in the pretty print.
     * @throws XMLStreamException If the XML is invalid.
     */
    private void writeSMP(XMLStreamWriter writer, SimpleMarshalledProvenance smp, int depth) throws XMLStreamException {
        if (prettyPrint) {
            for (int i = 0; i < depth; i++) {
                writer.writeCharacters("\t");
            }
        }
        writer.writeEmptyElement(SIMPLE_MARSHALLED_PROVENANCE);
        writer.writeAttribute(PROV_KEY, smp.getKey());
        writer.writeAttribute(PROV_VALUE, smp.getValue());
        writer.writeAttribute(PROV_ADDITIONAL, smp.getAdditional());
        writer.writeAttribute(PROVENANCE_CLASS_NAME, smp.getProvenanceClassName());
        writer.writeAttribute(IS_REFERENCE, ""+smp.isReference());
        if (prettyPrint) {
            writer.writeCharacters(System.lineSeparator());
        }
    }

    /**
     * Writes the supplied ListMarshalledProvenance to the XML stream.
     * @param writer The XML writer.
     * @param lmp The provenance to write.
     * @param depth The depth for tabbing in the pretty print.
     * @throws XMLStreamException If the XML is invalid.
     */
    private void writeLMP(XMLStreamWriter writer, String key, ListMarshalledProvenance lmp, int depth) throws XMLStreamException {
        if (prettyPrint) {
            for (int i = 0; i < depth; i++) {
                writer.writeCharacters("\t");
            }
        }
        if (lmp.getList().isEmpty()) {
            writer.writeEmptyElement(LIST_MARSHALLED_PROVENANCE);
            writer.writeAttribute(PROV_KEY, key);
        } else {
            writer.writeStartElement(LIST_MARSHALLED_PROVENANCE);
            writer.writeAttribute(PROV_KEY, key);
            if (prettyPrint) {
                writer.writeCharacters(System.lineSeparator());
            }
            for (FlatMarshalledProvenance fmp : lmp.getList()) {
                dispatchFMP(writer,"",fmp,depth+1);
            }
            if (prettyPrint) {
                for (int i = 0; i < depth; i++) {
                    writer.writeCharacters("\t");
                }
            }
            writer.writeEndElement();
        }
        if (prettyPrint) {
            writer.writeCharacters(System.lineSeparator());
        }
    }

    /**
     * Writes the supplied MapMarshalledProvenance to the XML stream.
     * @param writer The XML writer.
     * @param mmp The provenance to write.
     * @param depth The depth for tabbing in the pretty print.
     * @throws XMLStreamException If the XML is invalid.
     */
    private void writeMMP(XMLStreamWriter writer, String key, MapMarshalledProvenance mmp, int depth) throws XMLStreamException {
        if (prettyPrint) {
            for (int i = 0; i < depth; i++) {
                writer.writeCharacters("\t");
            }
        }
        if (mmp.isEmpty()) {
            writer.writeEmptyElement(MAP_MARSHALLED_PROVENANCE);
            writer.writeAttribute(PROV_KEY, key);
        } else {
            writer.writeStartElement(MAP_MARSHALLED_PROVENANCE);
            writer.writeAttribute(PROV_KEY, key);
            if (prettyPrint) {
                writer.writeCharacters(System.lineSeparator());
            }
            for (Pair<String,FlatMarshalledProvenance> p : mmp) {
                dispatchFMP(writer,p.getA(),p.getB(),depth+1);
            }
            if (prettyPrint) {
                for (int i = 0; i < depth; i++) {
                    writer.writeCharacters("\t");
                }
            }
            writer.writeEndElement();
        }
        if (prettyPrint) {
            writer.writeCharacters(System.lineSeparator());
        }
    }

    /**
     * Dispatch on the type of FlatMarshalledProvenance.
     * @param writer The XML writer.
     * @param key The key of the containing provenance.
     * @param fmp The FMP to dispatch on.
     * @param depth The depth for tabbing in the pretty print.
     * @throws XMLStreamException If the XML is invalid.
     */
    private void dispatchFMP(XMLStreamWriter writer, String key, FlatMarshalledProvenance fmp, int depth) throws XMLStreamException {
        if (fmp instanceof SimpleMarshalledProvenance) {
            SimpleMarshalledProvenance smp = (SimpleMarshalledProvenance) fmp;
            writeSMP(writer,smp,depth);
        } else if (fmp instanceof ListMarshalledProvenance) {
            ListMarshalledProvenance lmp = (ListMarshalledProvenance) fmp;
            writeLMP(writer,key,lmp,depth);
        } else if (fmp instanceof MapMarshalledProvenance) {
            MapMarshalledProvenance mmp = (MapMarshalledProvenance) fmp;
            writeMMP(writer,key,mmp,depth);
        } else {
            throw new RuntimeException("Should not reach here, unexpected FlatMarshalledProvenance subclass " + fmp.getClass());
        }
    }
}

