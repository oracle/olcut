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

import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads configuration from an XML file
 */
public class SaxLoader {

    private static final Logger logger = Logger.getLogger(SaxLoader.class.getName());

    private URL curURL;

    private Queue<URL> urlQueue;

    private Map<String, RawPropertyData> rpdMap;

    private Map<String, RawPropertyData> existingRPD;
    
    private Map<String, SerializedObject> serializedObjects = new HashMap<>();
    
    private GlobalProperties globalProperties;

    /**
     * Creates a loader that will load from the given location.
     *
     * @param url the location to load
     * @param globalProperties the map of global properties
     */
    public SaxLoader(URL url, GlobalProperties globalProperties) {
        this(url, globalProperties, null);
    }

    /**
     * Creates a loader that will load from the given location.
     *
     * @param url the location to load
     * @param globalProperties the map of global properties
     * @param existingRPD the map of existing raw property data from previously
     * loaded configuration files, which we might want when overriding elements
     * in the configuration file that we're loading.
     */
    public SaxLoader(URL url, GlobalProperties globalProperties,
            Map<String, RawPropertyData> existingRPD) {
        this.urlQueue = new LinkedList<>();
        this.urlQueue.add(url);
        this.globalProperties = globalProperties;
        this.existingRPD = existingRPD;
    }

    /**
     * Creates a loader that will load from the given locations.
     *
     * @param urlQueue A queue of locations to load.
     * @param globalProperties The map of global properties.
     */
    public SaxLoader(Queue<URL> urlQueue, GlobalProperties globalProperties) {
        this(urlQueue,globalProperties,null);
    }

    /**
     * Creates a loader that will load from the given locations.
     *
     * @param urlQueue A queue of locations to load.
     * @param globalProperties The map of global properties.
     * @param existingRPD The map of existing raw property data from previously
     * loaded configuration files, which we might want when overriding elements
     * in the configuration files that we're loading.
     */
    public SaxLoader(Queue<URL> urlQueue, GlobalProperties globalProperties, Map<String, RawPropertyData> existingRPD) {
        this.urlQueue = urlQueue;
        this.globalProperties = globalProperties;
        this.existingRPD = existingRPD;
    }

    /**
     * Loads a set of configuration data from the location
     *
     * @return a map keyed by component name containing RawPropertyData objects
     * @throws IOException if an I/O or parse error occurs
     */
    public Map<String, RawPropertyData> load() throws PropertyException, IOException {
        rpdMap = new HashMap<>();
        InputStream is = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader xr = factory.newSAXParser().getXMLReader();
            ConfigHandler handler = new ConfigHandler();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            while (!urlQueue.isEmpty()) {
                curURL = urlQueue.poll();
                is = curURL.openStream();
                xr.parse(new InputSource(is));
                is.close();
            }
        } catch (SAXParseException e) {
            String msg = "Error while parsing line " + e.getLineNumber()
                    + " of " + curURL + ": " + e.getMessage();
            throw new PropertyException(e, msg);
        } catch (SAXException e) {
            throw new PropertyException(e, "Problem with XML: " + e);
        } catch (ParserConfigurationException | IOException e) {
            throw new PropertyException(e, e.getMessage());
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return rpdMap;
    }

    public Map<String, SerializedObject> getSerializedObjects() {
        return serializedObjects;
    }

    /**
     * A SAX XML Handler implementation that builds up the map of raw property
     * data objects
     */
    class ConfigHandler extends DefaultHandler {

        RawPropertyData rpd = null;

        Locator locator;

        List<Object> itemList = null;

        String itemListName = null;

        String mapName = null;

        Map<String,String> entryMap = null;

        StringBuilder curItem;

        boolean overriding;


        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            switch (qName) {
                case "config":
                    // nothing to do
                    break;
                case "component":
                    String curComponent = attributes.getValue("name");
                    String curType = attributes.getValue("type");
                    String override = attributes.getValue("inherit");
                    String export = attributes.getValue("export");
                    String entriesName = attributes.getValue("entries");
                    String serializedForm = attributes.getValue("serialized");
                    boolean exportable = export != null && Boolean.valueOf(export);
                    String imp = attributes.getValue("import");
                    boolean importable = imp != null && Boolean.valueOf(imp);
                    String lt = attributes.getValue("leasetime");
                    if (export == null && lt != null) {
                        throw new SAXParseException("lease timeout "
                                + lt
                                + " specified for component that"
                                + " does not have export set",
                                locator);
                    }
                    long leaseTime = -1;
                    if (lt != null) {
                        try {
                            leaseTime = Long.parseLong(lt);
                            if (leaseTime < 0) {
                                throw new SAXParseException("lease timeout "
                                        + lt
                                        + " must be greater than 0",
                                        locator);
                            }
                        } catch (NumberFormatException nfe) {
                            throw new SAXParseException("lease timeout "
                                    + lt + " must be a long",
                                    locator);
                        }
                    }

                    //
                    // Check for a badly formed component tag.
                    if (curComponent == null
                            || (curType == null && override == null)) {
                        throw new SAXParseException("component element must specify "
                                + "'name' and either 'type' or 'inherit' attributes",
                                locator);
                    }
                    if (override != null) {

                        //
                        // If we're overriding an existing type, then we should pull
                        // its property set, copy it and override it. Note that we're
                        // not doing any type checking here, so it's possible to specify
                        // a type for override that is incompatible with the specified
                        // properties. If that's the case, then things might get
                        // really weird. We'll log an override with a specified type
                        // just in case.
                        RawPropertyData spd = rpdMap.get(override);
                        if (spd == null) {
                            spd = existingRPD.get(override);
                            if (spd == null) {
                                throw new SAXParseException("Override for undefined component: "
                                        + override, locator);
                            }
                        }
                        if (curType != null && !curType.equals(spd.getClassName())) {
                            logger.log(Level.FINE, String.format("Overriding component %s with component %s, new type is %s overridden type was %s",
                                    spd.getName(), curComponent, curType, spd.getClassName()));
                        }
                        if (curType == null) {
                            curType = spd.getClassName();
                        }
                        rpd = new RawPropertyData(curComponent, curType,
                                spd.getProperties());
                        overriding = true;
                    } else {
                        if (rpdMap.get(curComponent) != null) {
                            throw new SAXParseException("duplicate definition for "
                                    + curComponent, locator);
                        }
                        rpd = new RawPropertyData(curComponent, curType, null);
                    }

                    //
                    // Set the lease time.
                    rpd.setExportable(exportable);
                    rpd.setImportable(importable);
                    rpd.setLeaseTime(leaseTime);
                    rpd.setEntriesName(entriesName);
                    rpd.setSerializedForm(serializedForm);
                    break;
                case "property": {
                    String name = attributes.getValue("name");
                    String value = attributes.getValue("value");
                    if (attributes.getLength() != 2 || name == null || value == null) {
                        throw new SAXParseException("property element must only have "
                                + "'name' and 'value' attributes",
                                locator);
                    }
                    if (rpd == null) {
                        // we are not in a component so add this to the global
                        // set of symbols
                        //String symbolName = "${" + name + "}"; // why should we warp the global props here
                        String symbolName = name;
                        globalProperties.setValue(symbolName, value);
                    } else if (rpd.contains(name) && !overriding) {
                        throw new SAXParseException("Duplicate property: " + name,
                                locator);
                    } else {
                        rpd.add(name, value);
                    }
                    break;
                }
                case "propertylist":
                    itemListName = attributes.getValue("name");
                    if (attributes.getLength() != 1 || itemListName == null) {
                        throw new SAXParseException("list element must only have "
                                + "the 'name' attribute", locator);
                    }
                    itemList = new ArrayList<>();
                    break;
                case "item":
                case "type":
                    if (attributes.getLength() != 0) {
                        throw new SAXParseException("unknown 'item' attribute",
                                locator);
                    } else if (itemList == null) {
                        throw new SAXParseException("'item' or 'type' elements must be inside a 'propertylist'", locator);
                    }
                    curItem = new StringBuilder();
                    break;
                case "propertymap":
                    mapName = attributes.getValue("name");
                    if (attributes.getLength() != 1 || mapName == null) {
                        throw new SAXParseException("map element must only have "
                                + "the 'name' attribute", locator);
                    }
                    entryMap = new HashMap<String, String>();
                    break;
                case "entry": {
                    String key = attributes.getValue("key");
                    String value = attributes.getValue("value");
                    if (attributes.getLength() != 2 || key == null || value == null) {
                        throw new SAXParseException("entry element must only have "
                                + "'key' and 'value' attributes", locator);
                    } else if (entryMap == null) {
                        throw new SAXParseException("entry element must be inside a map", locator);
                    } else if (entryMap.containsKey(key)) {
                        throw new SAXParseException("Repeated entry in map, key = " + key + " already exists", locator);
                    }
                    entryMap.put(key.trim(), value.trim());
                    break;
                }
                case "file": {
                    String name = attributes.getValue("name");
                    String value = attributes.getValue("value");
                    if (attributes.getLength() != 2 || name == null || value == null) {
                        throw new SAXParseException("file element must only have "
                                + "'name' and 'value' attributes", locator);
                    }
                    if (rpd == null) {
                        // we are not in a component so add this to the processing queue
                        try {
                            URL newURL = SaxLoader.class.getResource(value);
                            if (newURL == null) {
                                newURL = (new File(value)).toURI().toURL();
                            }
                            urlQueue.add(newURL);
                        } catch (MalformedURLException ex) {
                            throw new SAXParseException("Incorrectly formatted file element " + name + " with value " + value, locator, ex);
                        }
                    } else {
                        throw new SAXParseException("File element found inside a component: " + name,
                                locator);
                    }
                    break;
                }
                case "serialized": {
                    String name = attributes.getValue("name");
                    String type = attributes.getValue("type");
                    String location = attributes.getValue("location");
                    if ((attributes.getLength() != 3) || (name == null) || (type == null) || (location == null)) {
                        throw new SAXParseException("serialized element must only have 'name', 'type' and 'location' elements", locator);
                    }
                    serializedObjects.put(name, new SerializedObject(name, location, type));
                    break;
                }
                default:
                    throw new SAXParseException("Unknown element '" + qName + "'",
                            locator);
            }
        }


        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#characters(char[], int, int)
         */
        public void characters(char ch[], int start, int length)
                throws SAXParseException {
            if (curItem != null) {
                curItem.append(ch, start, length);
            }
        }


        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String localName, String qName)
                throws SAXParseException {
            switch (qName) {
                case "component":
                    rpdMap.put(rpd.getName(), rpd);
                    rpd = null;
                    overriding = false;
                    break;
                case "property":
                    // nothing to do
                    break;
                case "propertylist":
                    if (rpd.contains(itemListName)) {
                        throw new SAXParseException("Duplicate property: "
                                + itemListName, locator);
                    } else {
                        rpd.add(itemListName, itemList);
                        itemList = null;
                    }
                    break;
                case "item":
                    itemList.add(curItem.toString().trim());
                    curItem = null;
                    break;
                case "type":
                    try {
                        itemList.add(Class.forName(curItem.toString()));
                    } catch (ClassNotFoundException cnfe) {
                        throw new SAXParseException("Unable to find class "
                                + curItem.toString() + " in property list " + itemListName, locator);
                    }
                    break;
                case "propertymap":
                    if (rpd.contains(mapName)) {
                        throw new SAXParseException("Duplicate property: "
                                + mapName, locator);
                    } else {
                        rpd.add(mapName, entryMap);
                        entryMap = null;
                    }
                    break;
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
