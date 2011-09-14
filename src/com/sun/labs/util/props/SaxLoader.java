/*
 *
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */
package com.sun.labs.util.props;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Loads configuration from an XML file */
public class SaxLoader {

    private URL url;

    private Map<String, RawPropertyData> rpdMap;

    private Map<String, RawPropertyData> existingRPD;

    private GlobalProperties globalProperties;

    /**
     * Creates a loader that will load from the given location
     *
     * @param url              the location to load
     * @param globalProperties the map of global properties
     * @param existingRPD the map of existing raw property data from previously
     * loaded configuration files, which we might want when overriding elements
     * in the configuration file that we're loading.
     */
    public SaxLoader(URL url, GlobalProperties globalProperties) {
        this(url, globalProperties, null);
    }

    /**
     * Creates a loader that will load from the given location
     *
     * @param url              the location to load
     * @param globalProperties the map of global properties
     * @param existingRPD the map of existing raw property data from previously
     * loaded configuration files, which we might want when overriding elements
     * in the configuration file that we're loading.
     */
    public SaxLoader(URL url, GlobalProperties globalProperties,
                      Map<String, RawPropertyData> existingRPD) {
        this.url = url;
        this.globalProperties = globalProperties;
        this.existingRPD = existingRPD;
    }

    /**
     * Loads a set of configuration data from the location
     *
     * @return a map keyed by component name containing RawPropertyData objects
     * @throws IOException if an I/O or parse error occurs
     */
    public Map<String, RawPropertyData> load() throws IOException {
        rpdMap = new HashMap<String, RawPropertyData>();
        InputStream is = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader xr = factory.newSAXParser().getXMLReader();
            ConfigHandler handler = new ConfigHandler();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            is = url.openStream();
            xr.parse(new InputSource(is));
        } catch(SAXParseException e) {
            String msg = "Error while parsing line " + e.getLineNumber() +
                    " of " + url + ": " + e.getMessage();
            throw new IOException(msg);
        } catch(SAXException e) {
            throw new IOException("Problem with XML: " + e);
        } catch(ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return rpdMap;
    }

    /** A SAX XML Handler implementation that builds up the map of raw property data objects */
    class ConfigHandler extends DefaultHandler {

        RawPropertyData rpd = null;

        Locator locator;

        List itemList = null;

        String itemListName = null;

        StringBuilder curItem;

        boolean overriding;


        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName,
                                  Attributes attributes) throws SAXException {
            if(qName.equals("config")) {
            // nothing to do
            } else if(qName.equals("component")) {
                String curComponent = attributes.getValue("name");
                String curType = attributes.getValue("type");
                String override = attributes.getValue("inherit");
                String export = attributes.getValue("export");
                String entriesName = attributes.getValue("entries");
                boolean exportable = export != null && Boolean.valueOf(export);
                String imp = attributes.getValue("import");
                boolean importable = imp != null && Boolean.valueOf(imp);
                String lt = attributes.getValue("leasetime");
                if(export == null && lt != null) {
                    throw new SAXParseException("lease timeout " +
                                                lt +
                                                " specified for component that" +
                                                " does not have export set",
                                                locator);

                }
                long leaseTime = -1;
                if(lt != null) {
                    try {
                        leaseTime = Long.parseLong(lt);
                        if(leaseTime < 0) {
                            throw new SAXParseException("lease timeout " +
                                                        lt +
                                                        " must be greater than 0",
                                                        locator);
                        }
                    } catch(NumberFormatException nfe) {
                        throw new SAXParseException("lease timeout " +
                                                    lt + " must be a long",
                                                    locator);
                    }
                }

                //
                // Check for a badly formed component tag.
                if(curComponent == null ||
                        (curType == null && override == null)) {
                    throw new SAXParseException("component element must specify " +
                                                "'name' and either 'type' or 'inherit' attributes",
                                                locator);
                }
                if(curType == null) {

                    //
                    // If we're not using an existing type, we're inheriting from
                    // an already-defined component.
                    RawPropertyData spd = rpdMap.get(override);
                    if(spd == null) {
                        spd = existingRPD.get(override);
                        if(spd == null) {
                            throw new SAXParseException("Override for undefined component: " +
                                                        override, locator);
                        }
                    }
                    rpd = new RawPropertyData(curComponent, spd.getClassName(),
                                              spd.getProperties());
                    overriding = true;
                } else {
                    if(rpdMap.get(curComponent) != null) {
                        throw new SAXParseException("duplicate definition for " +
                                                    curComponent, locator);
                    }
                    rpd = new RawPropertyData(curComponent, curType, null);
                }

                //
                // Set the lease time.
                rpd.setExportable(exportable);
                rpd.setImportable(importable);
                rpd.setLeaseTime(leaseTime);
                rpd.setEntriesName(entriesName);
            } else if(qName.equals("property")) {
                String name = attributes.getValue("name");
                String value = attributes.getValue("value");
                if(attributes.getLength() != 2 || name == null || value == null) {
                    throw new SAXParseException("property element must only have " +
                                                "'name' and 'value' attributes",
                                                locator);
                }
                if(rpd == null) {
                    // we are not in a component so add this to the global
                    // set of symbols
//                    String symbolName = "${" + name + "}"; // why should we warp the global props here
                    String symbolName = name;
                    globalProperties.setValue(symbolName, value);
                } else if(rpd.contains(name) && !overriding) {
                    throw new SAXParseException("Duplicate property: " + name,
                                                locator);
                } else {
                    rpd.add(name, value);
                }
            } else if(qName.equals("propertylist")) {
                itemListName = attributes.getValue("name");
                if(attributes.getLength() != 1 || itemListName == null) {
                    throw new SAXParseException("list element must only have " +
                                                "the 'name'  attribute", locator);
                }
                itemList = new ArrayList();
            } else if(qName.equals("item") || qName.equals("type")) {
                if(attributes.getLength() != 0) {
                    throw new SAXParseException("unknown 'item' attribute",
                                                locator);
                }
                curItem = new StringBuilder();
            } else {
                throw new SAXParseException("Unknown element '" + qName + "'",
                                            locator);
            }
        }


        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#characters(char[], int, int)
         */
        public void characters(char ch[], int start, int length)
                throws SAXParseException {
            if(curItem != null) {
                curItem.append(ch, start, length);
            }
        }


        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String localName, String qName)
                throws SAXParseException {
            if(qName.equals("component")) {
                rpdMap.put(rpd.getName(), rpd);
                rpd = null;
                overriding = false;
            } else if(qName.equals("property")) {
            // nothing to do
            } else if(qName.equals("propertylist")) {
                if(rpd.contains(itemListName)) {
                    throw new SAXParseException("Duplicate property: " +
                                                itemListName, locator);
                } else {
                    rpd.add(itemListName, itemList);
                    itemList = null;
                }
            } else if(qName.equals("item")) {
                itemList.add(curItem.toString().trim());
                curItem = null;
            } else if(qName.equals("type")) {
                try {
                itemList.add(Class.forName(curItem.toString()));

                } catch (ClassNotFoundException cnfe) {
                    throw new SAXParseException("Unable to find class " +
                            curItem.toString() + " in property list " + itemListName, locator);
                }
            }
        }


        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
         */
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }
    }
}
