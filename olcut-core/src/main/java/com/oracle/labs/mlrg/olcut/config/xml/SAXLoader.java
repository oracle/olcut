/*
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
package com.oracle.labs.mlrg.olcut.config.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.oracle.labs.mlrg.olcut.config.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.RawPropertyData;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.config.URLLoader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads configuration from an XML file.
 */
public class SAXLoader implements ConfigLoader {

    private static final Logger logger = Logger.getLogger(SAXLoader.class.getName());

    private final URLLoader parent;

    private final Map<String, RawPropertyData> rpdMap;

    private final Map<String, RawPropertyData> existingRPD;
    
    private final Map<String, SerializedObject> serializedObjects;
    
    private final GlobalProperties globalProperties;

    private final XMLReader xr;

    private final ConfigSAXHandler handler;

    public SAXLoader(URLLoader parent, Map<String, RawPropertyData> rpdMap, Map<String, RawPropertyData> existingRPD,
                     Map<String, SerializedObject> serializedObjects, GlobalProperties globalProperties) throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        xr = factory.newSAXParser().getXMLReader();
        ConfigSAXHandler handler = new ConfigSAXHandler();
        this.handler = handler;
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);

        this.parent = parent;
        this.rpdMap = rpdMap;
        this.existingRPD = existingRPD;
        this.serializedObjects = serializedObjects;
        this.globalProperties = globalProperties;
    }

    /**
     * Loads a set of configuration data from the location
     *
     * @throws IOException if an I/O or parse error occurs
     */
    @Override
    public void load(URL url) throws ConfigLoaderException, IOException {
        InputStream is = null;
        try {
            if (url.getProtocol().equals("file")) {
                String workingDir = new File(url.getFile()).getParent();
                handler.setCurWorkingDir(workingDir);
            } else {
                handler.setCurWorkingDir("");
            }
            is = url.openStream();
            xr.parse(new InputSource(is));
            is.close();
        } catch (SAXParseException e) {
            String msg = "Error while parsing line " + e.getLineNumber()
                    + " of " + url + ": " + e.getMessage();
            throw new ConfigLoaderException(e, msg);
        } catch (SAXException e) {
            throw new ConfigLoaderException(e, "Problem with XML: " + e);
        } catch (IOException e) {
            throw new ConfigLoaderException(e, e.getMessage());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @Override
    public String getExtension() {
        return "xml";
    }

    public Map<String, RawPropertyData> getPropertyMap() {
        return rpdMap;
    }

    public Map<String, SerializedObject> getSerializedObjects() {
        return serializedObjects;
    }

    public GlobalProperties getGlobalProperties() {
        return globalProperties;
    }

    /**
     * A SAX XML Handler implementation that builds up the map of raw property
     * data objects
     */
    private class ConfigSAXHandler extends DefaultHandler {

        RawPropertyData rpd = null;

        Locator locator;

        List<SimpleProperty> itemList = null;

        List<Class<?>> classList = null;

        String itemListName = null;

        String mapName = null;

        Map<String, SimpleProperty> entryMap = null;

        StringBuilder curItem;

        boolean overriding;

        String curWorkingDir;

        public void setCurWorkingDir(String curWorkingDir) {
            this.curWorkingDir = curWorkingDir;
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            switch (qName) {
                case CONFIG:
                    // nothing to do
                    break;
                case COMPONENT:
                    String curComponent = attributes.getValue(ConfigLoader.NAME);
                    String curType = attributes.getValue(ConfigLoader.TYPE);
                    String override = attributes.getValue(ConfigLoader.INHERIT);
                    String export = attributes.getValue(ConfigLoader.EXPORT);
                    String entriesName = attributes.getValue(ConfigLoader.ENTRIES);
                    String serializedForm = attributes.getValue(ConfigLoader.SERIALIZED);
                    boolean exportable = export != null && Boolean.valueOf(export);
                    String imp = attributes.getValue(ConfigLoader.IMPORT);
                    boolean importable = imp != null && Boolean.valueOf(imp);
                    String lt = attributes.getValue(ConfigLoader.LEASETIME);
                    if (export == null && lt != null) {
                        throw new SAXParseException("lease timeout "
                                + lt
                                + " specified for component that"
                                + " does not have export set",
                                locator);
                    }
                    long leaseTime = RawPropertyData.DEFAULT_LEASE_TIME;
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
                        rpd = new RawPropertyData(curComponent, curType, spd.getProperties(), serializedForm, entriesName, exportable, importable, leaseTime);
                        overriding = true;
                    } else {
                        if (rpdMap.get(curComponent) != null) {
                            throw new SAXParseException("duplicate definition for " + curComponent, locator);
                        }
                        rpd = new RawPropertyData(curComponent, curType, serializedForm, entriesName, exportable, importable, leaseTime);
                    }
                    break;
                case PROPERTY: {
                    String name = attributes.getValue(ConfigLoader.NAME);
                    String value = attributes.getValue(ConfigLoader.VALUE);
                    if (attributes.getLength() != 2 || name == null || value == null) {
                        throw new SAXParseException("property element must only have "
                                + "'name' and 'value' attributes", locator);
                    }
                    if (rpd == null) {
                        // we are not in a component so add this to the global
                        // set of symbols
                        //String symbolName = "${" + name + "}"; // why should we warp the global props here
                        try {
                            globalProperties.setValue(name, value);
                        } catch (PropertyException e) {
                            throw new SAXParseException("Invalid global property name: " + name, locator);
                        }
                    } else if (rpd.contains(name) && !overriding) {
                        throw new SAXParseException("Duplicate property: " + name, locator);
                    } else {
                        rpd.add(name, new SimpleProperty(value));
                    }
                    break;
                }
                case PROPERTYLIST:
                    itemListName = attributes.getValue(ConfigLoader.NAME);
                    if (attributes.getLength() != 1 || itemListName == null) {
                        throw new SAXParseException("list element must only have "
                                + "the 'name' attribute", locator);
                    }
                    itemList = new ArrayList<>();
                    classList = new ArrayList<>();
                    break;
                case ITEM:
                case TYPE:
                    if (attributes.getLength() != 0) {
                        throw new SAXParseException("unknown 'item' attribute", locator);
                    } else if (itemList == null) {
                        throw new SAXParseException("'item' or 'type' elements must be inside a 'propertylist'", locator);
                    }
                    curItem = new StringBuilder();
                    break;
                case PROPERTYMAP:
                    mapName = attributes.getValue(ConfigLoader.NAME);
                    if (attributes.getLength() != 1 || mapName == null) {
                        throw new SAXParseException("map element must only have "
                                + "the 'name' attribute", locator);
                    }
                    entryMap = new HashMap<>();
                    break;
                case ENTRY: {
                    String key = attributes.getValue(ConfigLoader.KEY);
                    String value = attributes.getValue(ConfigLoader.VALUE);
                    if (attributes.getLength() != 2 || key == null || value == null) {
                        throw new SAXParseException("entry element must only have "
                                + "'key' and 'value' attributes", locator);
                    } else if (entryMap == null) {
                        throw new SAXParseException("entry element must be inside a map", locator);
                    } else if (entryMap.containsKey(key)) {
                        throw new SAXParseException("Repeated entry in map, key = " + key + " already exists", locator);
                    }
                    entryMap.put(key.trim(), new SimpleProperty(value.trim()));
                    break;
                }
                case FILE: {
                    String name = attributes.getValue(ConfigLoader.NAME);
                    String value = attributes.getValue(ConfigLoader.VALUE);
                    if (attributes.getLength() != 2 || name == null || value == null) {
                        throw new SAXParseException("file element must only have "
                                + "'name' and 'value' attributes", locator);
                    }
                    if (rpd == null) {
                        // we are not in a component so add this to the processing queue
                        try {
                            URL newURL = ConfigurationManager.class.getResource(value);
                            if (newURL == null) {
                                File newFile = new File(value);
                                if (!newFile.isAbsolute()) {
                                    newFile = new File(curWorkingDir,value);
                                }
                                newURL = newFile.toURI().toURL();
                            }
                            parent.addURL(newURL);
                        } catch (MalformedURLException ex) {
                            throw new SAXParseException("Incorrectly formatted file element " + name + " with value " + value, locator, ex);
                        }
                    } else {
                        throw new SAXParseException("File element found inside a component: " + name,
                                locator);
                    }
                    break;
                }
                case SERIALIZED: {
                    String name = attributes.getValue(ConfigLoader.NAME);
                    String type = attributes.getValue(ConfigLoader.TYPE);
                    String location = attributes.getValue(ConfigLoader.LOCATION);
                    if ((attributes.getLength() != 3) || (name == null) || (type == null) || (location == null)) {
                        throw new SAXParseException("serialized element must only have 'name', 'type' and 'location' elements", locator);
                    }
                    serializedObjects.put(name, new SerializedObject(name, location, type));
                    break;
                }
                default:
                    throw new SAXParseException("Unknown element '" + qName + "'", locator);
            }
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#characters(char[], int, int)
         */
        public void characters(char[] ch, int start, int length) {
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
                case COMPONENT:
                    rpdMap.put(rpd.getName(), rpd);
                    rpd = null;
                    overriding = false;
                    break;
                case PROPERTY:
                    // nothing to do
                    break;
                case PROPERTYLIST:
                    if (rpd.contains(itemListName) && !overriding) {
                        throw new SAXParseException("Duplicate property: "
                                + itemListName, locator);
                    } else {
                        rpd.add(itemListName, new ListProperty(itemList,classList));
                        itemList = null;
                        classList = null;
                    }
                    break;
                case ITEM:
                    itemList.add(new SimpleProperty(curItem.toString().trim()));
                    curItem = null;
                    break;
                case TYPE:
                    try {
                        classList.add(Class.forName(curItem.toString()));
                    } catch (ClassNotFoundException cnfe) {
                        throw new SAXParseException("Unable to find class "
                                + curItem.toString() + " in property list "
                                + itemListName, locator);
                    }
                    break;
                case PROPERTYMAP:
                    if (rpd.contains(mapName) && !overriding) {
                        throw new SAXParseException("Duplicate property: "
                                + mapName, locator);
                    } else {
                        rpd.add(mapName, new MapProperty(entryMap));
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
