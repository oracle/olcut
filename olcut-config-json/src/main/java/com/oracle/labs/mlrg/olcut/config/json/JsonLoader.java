/*
 * Copyright (c) 2018, 2025, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.config.io.URLLoader;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ConfigLoader} for configuration data stored in json format.
 */
public final class JsonLoader implements ConfigLoader {

    private static final Logger logger = Logger.getLogger(JsonLoader.class.getName());

    private final JsonFactory factory;

    private final URLLoader parent;

    private final Map<String, ConfigurationData> rpdMap;

    private final Map<String, ConfigurationData> existingRPD;

    private final Map<String, SerializedObject> serializedObjects;

    private final GlobalProperties globalProperties;

    private String workingDir;

    /**
     * Constructs a configuration loader for JSON formatted data.
     * @param factory The JsonFactory to use.
     * @param parent The parent URL loader for chaining.
     * @param rpdMap The current property map.
     * @param existingRPD Any existing property maps.
     * @param serializedObjects The existing serialized objects.
     * @param globalProperties The existing global properties.
     */
    public JsonLoader(JsonFactory factory, URLLoader parent, Map<String, ConfigurationData> rpdMap, Map<String, ConfigurationData> existingRPD,
                      Map<String, SerializedObject> serializedObjects, GlobalProperties globalProperties) {
        this.factory = factory;
        this.parent = parent;
        this.rpdMap = rpdMap;
        this.existingRPD = existingRPD;
        this.serializedObjects = serializedObjects;
        this.globalProperties = globalProperties;
    }

    /**
     * Loads json configuration data from the location
     */
    @Override
    public void load(URL url) throws ConfigLoaderException {
        if (url.getProtocol().equals("file")) {
            workingDir = new File(url.getFile()).getParent();
        } else if (IOUtil.isDisallowedProtocol(url)) {
            throw new ConfigLoaderException("Unable to load configurations from URLs with protocol: " + url.getProtocol());
        } else {
            workingDir = "";
        }
        try (JsonParser parser = factory.createParser(url)) {
            parser.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
            parseJson(parser);
        } catch (IOException e) {
            String msg = "Error while parsing " + url.toString() + ": " + e.getMessage();
            throw new ConfigLoaderException(e, msg);
        }
    }

    /**
     * Loads json configuration data from the stream
     */
    @Override
    public void load(InputStream stream) throws ConfigLoaderException {
        try (JsonParser parser = factory.createParser(stream)) {
            parser.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
            parseJson(parser);
        } catch (IOException e) {
            String msg = "Error while parsing input: " + e.getMessage();
            throw new ConfigLoaderException(e, msg);
        }
    }

    private void parseJson(JsonParser parser) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            parser.nextToken(); // now currentToken == START_OBJECT
            if (parser.nextToken() == null) {
                throw new ConfigLoaderException("Failed to parse JSON, did not start with " + ConfigLoader.CONFIG + " object.");
            } // now currentToken == CONFIG
            if (parser.currentName().equals(ConfigLoader.CONFIG)) {
                parser.nextToken();
                ObjectNode node = mapper.readTree(parser);
                ObjectNode globalPropertiesNode = (ObjectNode) node.get(ConfigLoader.GLOBALPROPERTIES);
                ArrayNode filesNode = (ArrayNode) node.get(ConfigLoader.FILES);
                ArrayNode serializedObjectsNode = (ArrayNode) node.get(ConfigLoader.SERIALIZEDOBJECTS);
                ArrayNode componentsNode = (ArrayNode) node.get(ConfigLoader.COMPONENTS);
                if (globalPropertiesNode != null) {
                    Iterator<Entry<String,JsonNode>> itr = globalPropertiesNode.fields();
                    while (itr.hasNext()) {
                        Entry<String,JsonNode> e = itr.next();
                        try {
                            globalProperties.setValue(e.getKey(), e.getValue().textValue());
                        } catch (PropertyException ex) {
                            throw new ConfigLoaderException("Invalid global property name: " + e.getKey());
                        }
                    }
                }
                if (filesNode != null) {
                    for (JsonNode file : filesNode) {
                        parseFile((ObjectNode)file);
                    }
                }
                if (serializedObjectsNode != null) {
                    for (JsonNode serialized : serializedObjectsNode) {
                        parseSerializedObject((ObjectNode)serialized);
                    }
                }
                if (componentsNode != null) {
                    for (JsonNode component : componentsNode) {
                        parseComponent((ObjectNode)component);
                    }
                }

            } else {
                throw new ConfigLoaderException("Did not start with " + ConfigLoader.CONFIG + " object.");
            }
        } catch (IOException | ClassCastException e) {
            throw new ConfigLoaderException(e);
        }
    }

    private void parseComponent(ObjectNode node) {
        boolean overriding = false;
        JsonNode curComponentNode = node.get(ConfigLoader.NAME);
        JsonNode curTypeNode = node.get(ConfigLoader.TYPE);
        JsonNode overrideNode = node.get(ConfigLoader.INHERIT);
        //
        // Check for a badly formed component tag.
        if (curComponentNode == null || (curTypeNode == null && overrideNode == null)) {
            throw new ConfigLoaderException("Component element must specify "
                    + "'name' and either 'type' or 'inherit' attributes, found " + node.toString());
        }
        String curComponent = curComponentNode.textValue();
        String curType = curTypeNode != null ? curTypeNode.textValue() : null;
        String override = overrideNode != null ? overrideNode.textValue() : null;

        JsonNode serializedFormNode = node.get(ConfigLoader.SERIALIZED);
        String serializedForm = serializedFormNode != null ? serializedFormNode.textValue() : null;

        ConfigurationData rpd;
        if (override != null) {
            //
            // If we're overriding an existing type, then we should pull
            // its property set, copy it and override it. Note that we're
            // not doing any type checking here, so it's possible to specify
            // a type for override that is incompatible with the specified
            // properties. If that's the case, then things might get
            // really weird. We'll log an override with a specified type
            // just in case.
            ConfigurationData spd = rpdMap.get(override);
            if (spd == null) {
                if (existingRPD == null || !existingRPD.containsKey(override)) {
                    throw new ConfigLoaderException("Failed to find base component '"+override+"' inherited from '"+curComponent+"'.");
                } else {
                    spd = existingRPD.get(override);
                }
            }
            if (curType != null && !curType.equals(spd.className())) {
                logger.log(Level.FINE, String.format("Overriding component %s with component %s, new type is %s overridden type was %s",
                        spd.name(), curComponent, curType, spd.className()));
            }
            if (curType == null) {
                curType = spd.className();
            }
            rpd = new ConfigurationData(curComponent, curType, spd.properties(), serializedForm);
            overriding = true;
        } else {
            if (rpdMap.get(curComponent) != null) {
                throw new ConfigLoaderException("duplicate definition for "
                        + curComponent);
            }
            rpd = new ConfigurationData(curComponent, curType, serializedForm);
        }

        ObjectNode properties = (ObjectNode) node.get(ConfigLoader.PROPERTIES);
        // properties is null if there are no properties specified in the json
        if (properties != null) {
            Iterator<Entry<String, JsonNode>> fieldsItr = properties.fields();
            while (fieldsItr.hasNext()) {
                Entry<String, JsonNode> e = fieldsItr.next();
                String propName = e.getKey();
                if (e.getValue() instanceof ArrayNode listNode) {
                    // Must be list
                    ArrayList<SimpleProperty> listOutput = new ArrayList<>();
                    ArrayList<Class<?>> classListOutput = new ArrayList<>();
                    for (JsonNode element : listNode) {
                        if (element.size() > 1) {
                            throw new ConfigLoaderException("Too many elements in a propertylist item, found " + element);
                        }
                        Iterator<Entry<String, JsonNode>> listElementItr = element.fields();
                        while (listElementItr.hasNext()) {
                            Entry<String, JsonNode> elementEntry = listElementItr.next();
                            String elementName = elementEntry.getKey();
                            if (elementEntry.getValue().isTextual()) {
                                String value = elementEntry.getValue().textValue();
                                switch (elementName) {
                                    case ConfigLoader.ITEM:
                                        listOutput.add(new SimpleProperty(value));
                                        break;
                                    case ConfigLoader.TYPE:
                                        try {
                                            classListOutput.add(Class.forName(value));
                                        } catch (ClassNotFoundException cnfe) {
                                            throw new ConfigLoaderException("Unable to find class "
                                                    + value + " in component " + curComponent + ", propertylist " + propName);
                                        }
                                        break;
                                    default:
                                        throw new ConfigLoaderException("Unknown node in component " + curComponent + ", propertylist " + propName + ", node = " + e.getValue().toString());
                                }
                            } else {
                                throw new ConfigLoaderException("Invalid value in component " + curComponent + ", propertylist " + propName + ", node = " + e.getValue().toString() + "" +
                                        ", all OLCUT property list values must be strings, other types are not parsed.");
                            }
                        }
                    }
                    ListProperty listProp;
                    if (classListOutput.isEmpty()) {
                        listProp = new ListProperty(listOutput);
                    } else {
                        listProp = new ListProperty(listOutput,classListOutput);
                    }
                    rpd.add(propName, listProp);
                } else if (e.getValue() instanceof ObjectNode) {
                    // Must be map
                    Map<String, SimpleProperty> mapOutput = new HashMap<>();
                    Iterator<Entry<String, JsonNode>> mapElementItr = e.getValue().fields();
                    while (mapElementItr.hasNext()) {
                        Entry<String, JsonNode> mapEntry = mapElementItr.next();
                        if (mapEntry.getValue().isTextual()) {
                            mapOutput.put(mapEntry.getKey(), new SimpleProperty(mapEntry.getValue().textValue()));
                        } else {
                            throw new ConfigLoaderException("Invalid value in component " + curComponent + ", propertymap " + propName + ", node = " + e.getValue().toString() +
                                    ", all OLCUT property map values must be strings, other types are not parsed.");
                        }
                    }
                    rpd.add(propName, new MapProperty(mapOutput));
                } else {
                    // Generic property.
                    if (e.getValue().isTextual()) {
                        rpd.add(propName, new SimpleProperty(e.getValue().textValue()));
                    } else {
                        throw new ConfigLoaderException("Invalid value in component " + curComponent + ", property " + propName + ", node = " + e.getValue().toString() +
                                ", all OLCUT property values must be strings, other types are not parsed.");
                    }
                }
            }
        }
        rpdMap.put(rpd.name(),rpd);
    }

    private void parseFile(ObjectNode node) {
        JsonNode name = node.get(ConfigLoader.NAME);
        JsonNode value = node.get(ConfigLoader.VALUE);
        if (name == null || value == null) {
            throw new ConfigLoaderException("File element must have "
                    + "'name' and 'value' attributes, found " + node.toString());
        }
        try {
            String path = value.textValue();
            URL newURL = ConfigurationManager.class.getResource(path);
            if (newURL == null) {
                File newFile = new File(path);
                if (!newFile.isAbsolute()) {
                    newFile = new File(workingDir,path);
                }
                newURL = newFile.toURI().toURL();
            }
            parent.addURL(newURL);
        } catch (MalformedURLException ex) {
            throw new ConfigLoaderException(ex, "Incorrectly formatted file element " + name.textValue() + " with value " + value.textValue());
        }
    }

    private void parseSerializedObject(ObjectNode node) {
        JsonNode name = node.get(ConfigLoader.NAME);
        JsonNode type = node.get(ConfigLoader.TYPE);
        JsonNode location = node.get(ConfigLoader.LOCATION);
        if ((name == null) || (type == null) || (location == null)) {
            throw new ConfigLoaderException("Serialized element must have 'name', 'type' and 'location' elements, found " + node.toString());
        }
        serializedObjects.put(name.textValue(), new SerializedObject(name.textValue(), location.textValue(), type.textValue()));
    }
}
