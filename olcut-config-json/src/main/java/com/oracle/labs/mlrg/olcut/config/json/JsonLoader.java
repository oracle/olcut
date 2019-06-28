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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class JsonLoader implements ConfigLoader {

    private static final Logger logger = Logger.getLogger(JsonLoader.class.getName());

    private final JsonFactory factory;

    private final URLLoader parent;

    private final Map<String, ConfigurationData> rpdMap;

    private final Map<String, ConfigurationData> existingRPD;

    private final Map<String, SerializedObject> serializedObjects;

    private final GlobalProperties globalProperties;

    private String workingDir;

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
        AccessController.doPrivileged((PrivilegedAction<Void>)
                () -> {
                    if (url.getProtocol().equals("file")) {
                        workingDir = new File(url.getFile()).getParent();
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
                    return null;
                }
        );
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

    public Map<String, ConfigurationData> getPropertyMap() {
        return rpdMap;
    }

    public Map<String, SerializedObject> getSerializedObjects() {
        return serializedObjects;
    }

    public GlobalProperties getGlobalProperties() {
        return globalProperties;
    }

    protected void parseJson(JsonParser parser) {
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

    protected void parseComponent(ObjectNode node) {
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

        JsonNode export = node.get(ConfigLoader.EXPORT);
        boolean exportable = export != null && Boolean.valueOf(export.textValue());
        JsonNode impNode = node.get(ConfigLoader.IMPORT);
        boolean importable = impNode != null && Boolean.valueOf(impNode.textValue());
        JsonNode lt = node.get(ConfigLoader.LEASETIME);
        if (export == null && lt != null) {
            throw new ConfigLoaderException("lease timeout " + lt +
                    " specified for component that does not have export set, at node " + node.toString());
        }
        long leaseTime = ConfigurationData.DEFAULT_LEASE_TIME;
        if (lt != null) {
            try {
                leaseTime = Long.parseLong(lt.textValue());
                if (leaseTime < 0) {
                    throw new ConfigLoaderException("lease timeout "
                            + lt + " must be greater than 0, for component " + curComponent);
                }
            } catch (NumberFormatException nfe) {
                throw new ConfigLoaderException("lease timeout "
                        + lt + " must be a long, for component " + curComponent);
            }
        }
        JsonNode entriesNameNode = node.get(ConfigLoader.ENTRIES);
        String entriesName = entriesNameNode != null ? entriesNameNode.textValue() : null;
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
                spd = existingRPD.get(override);
                if (spd == null) {
                    throw new ConfigLoaderException("Override for undefined component: "
                            + override + ", with name " + curComponent);
                }
            }
            if (curType != null && !curType.equals(spd.getClassName())) {
                logger.log(Level.FINE, String.format("Overriding component %s with component %s, new type is %s overridden type was %s",
                        spd.getName(), curComponent, curType, spd.getClassName()));
            }
            if (curType == null) {
                curType = spd.getClassName();
            }
            rpd = new ConfigurationData(curComponent, curType, spd.getProperties(), serializedForm, entriesName, exportable, importable, leaseTime);
            overriding = true;
        } else {
            if (rpdMap.get(curComponent) != null) {
                throw new ConfigLoaderException("duplicate definition for "
                        + curComponent);
            }
            rpd = new ConfigurationData(curComponent, curType, serializedForm, entriesName, exportable, importable, leaseTime);
        }

        ObjectNode properties = (ObjectNode) node.get(ConfigLoader.PROPERTIES);
        // properties is null if there are no properties specified in the json
        if (properties != null) {
            Iterator<Entry<String, JsonNode>> fieldsItr = properties.fields();
            while (fieldsItr.hasNext()) {
                Entry<String, JsonNode> e = fieldsItr.next();
                String propName = e.getKey();
                if (e.getValue() instanceof ArrayNode) {
                    // Must be list
                    ArrayList<SimpleProperty> listOutput = new ArrayList<>();
                    ArrayList<Class<?>> classListOutput = new ArrayList<>();
                    ArrayNode listNode = (ArrayNode) e.getValue();
                    for (JsonNode element : listNode) {
                        if (element.size() > 1) {
                            throw new ConfigLoaderException("Too many elements in a propertylist item, found " + element);
                        }
                        Iterator<Entry<String, JsonNode>> listElementItr = element.fields();
                        while (listElementItr.hasNext()) {
                            Entry<String, JsonNode> elementEntry = listElementItr.next();
                            String elementName = elementEntry.getKey();
                            switch (elementName) {
                                case ConfigLoader.ITEM:
                                    listOutput.add(new SimpleProperty(elementEntry.getValue().textValue()));
                                    break;
                                case ConfigLoader.TYPE:
                                    try {
                                        classListOutput.add(Class.forName(elementEntry.getValue().textValue()));
                                    } catch (ClassNotFoundException cnfe) {
                                        throw new ConfigLoaderException("Unable to find class "
                                                + elementEntry.getValue().textValue() + " in component " + curComponent + ", propertylist " + propName);
                                    }
                                    break;
                                default:
                                    throw new ConfigLoaderException("Unknown node in component " + curComponent + ", propertylist " + propName + ", node = " + e.getValue().toString());
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
                            throw new ConfigLoaderException("Unknown node in component " + curComponent + ", propertymap " + propName + ", node = " + e.getValue().toString());
                        }
                    }
                    rpd.add(propName, new MapProperty(mapOutput));
                } else {
                    // Generic property.
                    rpd.add(propName, new SimpleProperty(e.getValue().textValue()));
                }
            }
        }
        rpdMap.put(rpd.getName(),rpd);
    }

    protected void parseFile(ObjectNode node) {
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

    protected void parseSerializedObject(ObjectNode node) {
        JsonNode name = node.get(ConfigLoader.NAME);
        JsonNode type = node.get(ConfigLoader.TYPE);
        JsonNode location = node.get(ConfigLoader.LOCATION);
        if ((name == null) || (type == null) || (location == null)) {
            throw new ConfigLoaderException("Serialized element must have 'name', 'type' and 'location' elements, found " + node.toString());
        }
        serializedObjects.put(name.textValue(), new SerializedObject(name.textValue(), location.textValue(), type.textValue()));
    }
}
