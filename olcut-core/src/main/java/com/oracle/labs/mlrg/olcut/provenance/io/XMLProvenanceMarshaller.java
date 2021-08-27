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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
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

    private final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    private final XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();

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
    public List<ObjectMarshalledProvenance> deserializeFromFile(Path path) throws ProvenanceSerializationException, IOException {
        return parse(new InputSource(Files.newInputStream(path)),path.toString());
    }

    @Override
    public List<ObjectMarshalledProvenance> deserializeFromString(String input) throws ProvenanceSerializationException {
        try {
            InputSource source = new InputSource(new StringReader(input));
            return parse(source,"");
        } catch (IOException e) {
            throw new ProvenanceSerializationException("Found an IOException when reading from an in-memory string",e);
        }
    }

    /**
     * Parses the input stream into a list of ObjectMarshalledProvenances.
     * @param source The XML input source to parse.
     * @param location The file path if known.
     * @return The parsed provenances.
     * @throws ProvenanceSerializationException If the provenance could not be parsed from the file.
     * @throws IOException If the file failed to read.
     */
    private List<ObjectMarshalledProvenance> parse(InputSource source, String location) throws ProvenanceSerializationException, IOException {
        try {
            List<ObjectMarshalledProvenance> outputList = new ArrayList<>();
            XMLReader xr = saxFactory.newSAXParser().getXMLReader();
            ProvenanceSAXHandler handler = new ProvenanceSAXHandler(outputList);
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(source);
            return outputList;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Could not configure the SAX parser",e);
        } catch (SAXParseException e) {
            String msg;
            if (location != null && !location.isEmpty()) {
                msg = "Error while parsing line " + e.getLineNumber()
                        + " of " + location + ": " + e.getMessage();
            } else {
                msg = "Error while parsing line " + e.getLineNumber()
                        + " of input: " + e.getMessage();
            }
            throw new ProvenanceSerializationException(msg,e);
        } catch (SAXException e) {
            throw new ProvenanceSerializationException("Problem with XML: " + e,e);
        }
    }

    @Override
    public String serializeToString(List<ObjectMarshalledProvenance> marshalledProvenances) {
        try {
            StringWriter strWriter = new StringWriter();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(strWriter);
            writeProvenance(writer, marshalledProvenances);
            return strWriter.toString();
        } catch (XMLStreamException e) {
            throw new IllegalArgumentException("Failed to serialize to XML", e);
        }
    }

    @Override
    public void serializeToFile(List<ObjectMarshalledProvenance> marshalledProvenances, Path path) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(path))) {
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(bos, "utf-8");
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


    /**
     * A SAX XML Handler implementation that parses provenance xml files.
     */
    private static class ProvenanceSAXHandler extends DefaultHandler {

        private Locator locator;

        private final List<ObjectMarshalledProvenance> provenanceList;

        private Map<String, String> ompAttributeMap;
        private Map<String, FlatMarshalledProvenance> ompProvMap;
        private Deque<ProvCollection> provenanceChain;

        private SimpleMarshalledProvenance curSMP;
        private String curKey;

        /**
         * Create a new ProvenanceSAXHandler.
         */
        ProvenanceSAXHandler(List<ObjectMarshalledProvenance> provenanceList) {
            this.provenanceList = provenanceList;
            this.provenanceChain = new ArrayDeque<>();
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            switch (qName) {
                case PROVENANCES:
                    // nothing to do
                    break;
                case OBJECT_MARSHALLED_PROVENANCE: {
                    String curObjName = attributes.getValue(OBJECT_NAME);
                    String curObjClassName = attributes.getValue(OBJECT_CLASS_NAME);
                    String curProvClassName = attributes.getValue(PROVENANCE_CLASS_NAME);

                    //
                    // Check for a badly formed component tag.
                    if ((curObjName == null || curObjName.isEmpty()) || (curObjClassName == null || curObjClassName.isEmpty())
                            || (curProvClassName == null || curProvClassName.isEmpty())) {
                        throw new SAXParseException(String.format("%s element must specify "
                                + "'%s', '%s' and '%s' attributes", OBJECT_MARSHALLED_PROVENANCE, OBJECT_NAME, OBJECT_CLASS_NAME, PROVENANCE_CLASS_NAME),
                                locator);
                    }
                    ompAttributeMap = new HashMap<>();
                    ompProvMap = new HashMap<>();
                    ompAttributeMap.put(OBJECT_NAME, curObjName);
                    ompAttributeMap.put(OBJECT_CLASS_NAME, curObjClassName);
                    ompAttributeMap.put(PROVENANCE_CLASS_NAME, curProvClassName);
                    break;
                }
                case SIMPLE_MARSHALLED_PROVENANCE: {
                    String curKey = attributes.getValue(PROV_KEY);
                    String curValue = attributes.getValue(PROV_VALUE);
                    String curAdditional = attributes.getValue(PROV_ADDITIONAL);
                    String curProvClassName = attributes.getValue(PROVENANCE_CLASS_NAME);
                    boolean isReference = Boolean.parseBoolean(attributes.getValue(IS_REFERENCE));

                    if ((curKey == null || curKey.isEmpty()) || (curValue == null) || (curAdditional == null)
                            || (curProvClassName == null || curProvClassName.isEmpty())) {
                        throw new SAXParseException(String.format("%s element must specify "
                                + "'%s', '%s' and '%s' attributes", SIMPLE_MARSHALLED_PROVENANCE, PROV_KEY, PROV_VALUE, PROVENANCE_CLASS_NAME),
                                locator);
                    }

                    curSMP = new SimpleMarshalledProvenance(curKey,curValue,curProvClassName,isReference,curAdditional);
                    break;
                }
                case LIST_MARSHALLED_PROVENANCE:
                case MAP_MARSHALLED_PROVENANCE:
                    String curKey = attributes.getValue(PROV_KEY);
                    // This check is more complicated as it's ok for Map or List provenances to not have keys if they
                    // are the immediate children of list provenances.
                    if (curKey == null || (curKey.isEmpty() && (provenanceChain.isEmpty() || provenanceChain.peekLast().isMap()))) {
                        throw new SAXParseException(String.format("%s element must specify '%s'", qName, PROV_KEY), locator);
                    }
                    ProvCollection pc = new ProvCollection(curKey, qName.equals(MAP_MARSHALLED_PROVENANCE));
                    provenanceChain.addLast(pc);
                    break;
                default:
                    throw new SAXParseException("Unknown element '" + qName + "'", locator);
            }
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXParseException {
            switch (qName) {
                case PROVENANCES:
                    // nothing to do
                    break;
                case OBJECT_MARSHALLED_PROVENANCE:
                    ObjectMarshalledProvenance omp = new ObjectMarshalledProvenance(ompAttributeMap.get(OBJECT_NAME),
                            ompProvMap, ompAttributeMap.get(OBJECT_CLASS_NAME), ompAttributeMap.get(PROVENANCE_CLASS_NAME));
                    provenanceList.add(omp);
                    if (!provenanceChain.isEmpty()) {
                        throw new SAXParseException("Not cleared all the elements of the provenance chain when closing a ObjectMarshalledProvenance, found " + provenanceChain.size(),locator);
                    }
                    break;
                case SIMPLE_MARSHALLED_PROVENANCE:
                    if (curSMP != null) {
                        if (provenanceChain.isEmpty()) {
                            // Must be writing to the current ObjectMarshalledProvenance
                            ompProvMap.put(curSMP.getKey(),curSMP);
                        } else {
                            ProvCollection pc = provenanceChain.peekLast();
                            if (pc.isMap()) {
                                pc.addProvenance(curSMP.getKey(),curSMP);
                            } else {
                                pc.addProvenance(curSMP);
                            }
                        }
                        curSMP = null;
                    } else {
                        throw new SAXParseException("Found a SMP close without matching SMP open",locator);
                    }
                    break;
                case LIST_MARSHALLED_PROVENANCE:
                case MAP_MARSHALLED_PROVENANCE:
                    if (!provenanceChain.isEmpty()) {
                        Pair<String,FlatMarshalledProvenance> pair = provenanceChain.pollLast().emitProvenance();
                        if (provenanceChain.isEmpty()) {
                            // Must be in the root node
                            ompProvMap.put(pair.getA(),pair.getB());
                        } else {
                            ProvCollection pc = provenanceChain.peekLast();
                            if (pc.isMap()) {
                                pc.addProvenance(pair.getA(),pair.getB());
                            } else {
                                pc.addProvenance(pair.getB());
                            }
                        }
                    } else {
                        throw new SAXParseException("Found a LMP or MMP close without matching LMP/MMP open",locator);
                    }
                    break;
                default:
                    throw new SAXParseException("Unknown element '" + qName + "'", locator);
            }
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
         */
        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }
    }

    /**
     * A larval collection provenance to be used while descending in the depth-first search of the XML.
     */
    private static class ProvCollection {
        private final String key;
        private final boolean isMap;
        private final Map<String,FlatMarshalledProvenance> fmpMap;
        private final List<FlatMarshalledProvenance> fmpList;

        ProvCollection(String key, boolean isMap) {
            this.key = key;
            this.isMap = isMap;
            if (isMap) {
                fmpMap = new HashMap<>();
                fmpList = Collections.emptyList();
            } else {
                fmpMap = Collections.emptyMap();
                fmpList = new ArrayList<>();
            }
        }

        /**
         * Is this provCollection a map or not.
         * @return True if it is a map.
         */
        public boolean isMap() {
            return isMap;
        }

        /**
         * Adds a flat marshalled provenance to a list.
         * @param newFMP The provenance to add.
         */
        public void addProvenance(FlatMarshalledProvenance newFMP) {
            if (isMap) {
                throw new IllegalStateException("Added a list element to a map provenance");
            } else {
                fmpList.add(newFMP);
            }
        }

        /**
         * Adds a flat marshalled provenance to a map.
         * @param key The provenance key.
         * @param newFMP The provenance to add.
         */
        public void addProvenance(String key, FlatMarshalledProvenance newFMP) {
            if (isMap) {
                fmpMap.put(key,newFMP);
            } else {
                throw new IllegalStateException("Added a map element to a list provenance");
            }
        }

        /**
         * Creates the provenance after this element has been finalised.
         * @return The marshalled provenance object.
         */
        public Pair<String,FlatMarshalledProvenance> emitProvenance() {
            if (isMap) {
                return new Pair<>(key,new MapMarshalledProvenance(fmpMap));
            } else {
                return new Pair<>(key,new ListMarshalledProvenance(fmpList));
            }
        }
    }
}

