package com.oracle.labs.mlrg.olcut.config.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oracle.labs.mlrg.olcut.config.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.RawPropertyData;
import com.oracle.labs.mlrg.olcut.config.URLLoader;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class JsonLoader implements ConfigLoader {

    private static final Logger logger = Logger.getLogger(JsonLoader.class.getName());

    public static final String CONFIG_TYPE = "config-type";
    public static final String MEMBER = "member";
    public static final String LIST = "list";

    private final JsonFactory factory;

    private final URLLoader parent;

    private final Map<String, RawPropertyData> rpdMap;

    private final Map<String, RawPropertyData> existingRPD;

    private final Map<String, SerializedObject> serializedObjects;

    private final GlobalProperties globalProperties;

    public JsonLoader(JsonFactory factory, URLLoader parent, Map<String, RawPropertyData> rpdMap, Map<String, RawPropertyData> existingRPD,
                     Map<String, SerializedObject> serializedObjects, GlobalProperties globalProperties) {
        this.factory = factory;
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
        JsonParser parser = factory.createParser(url);
        parseJson(parser);
        parser.close();
    }

    @Override
    public String getExtension() {
        return "json";
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

    protected void parseJson(JsonParser parser) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            parser.nextToken(); // now currentToken == START_OBJECT
            parser.nextToken(); // now currentToken == CONFIG
            if (parser.currentName().equals(ConfigLoader.CONFIG)) {
                if (parser.nextToken() == JsonToken.START_ARRAY) {
                    while (parser.nextToken() == JsonToken.START_OBJECT) {
                        ObjectNode node = mapper.readTree(parser);
                        String nodeType = node.get(JsonLoader.CONFIG_TYPE).textValue();
                        switch (nodeType) {
                            case PROPERTY:
                                parseGlobalProperty(node);
                                break;
                            case FILE:
                                parseFile(node);
                                break;
                            case COMPONENT:
                                parseComponent(node);
                                break;
                            case SERIALIZED:
                                parseSerializedObject(node);
                                break;
                            default:
                                throw new ConfigLoaderException("Unknown node type " + nodeType);
                        }
                    }
                } else {
                    throw new ConfigLoaderException("Did not start with " + ConfigLoader.CONFIG + " array.");
                }
            } else {
                throw new ConfigLoaderException("Did not start with " + ConfigLoader.CONFIG + " array.");
            }
        } catch (IOException | ClassCastException e) {
            throw new ConfigLoaderException(e);
        }
    }

    protected void parseGlobalProperty(ObjectNode node) {
        JsonNode name = node.get(ConfigLoader.NAME);
        JsonNode value = node.get(ConfigLoader.VALUE);
        if (name == null || value == null) {
            throw new ConfigLoaderException("Property element must have "
                    + "'name' and 'value' attributes, found " + node.toString());
        }
        globalProperties.setValue(name.textValue(), value.textValue());
    }

    protected void parseComponent(ObjectNode node) {
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
        long leaseTime = -1;
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
        JsonNode serializedFormNode = node.get(ConfigLoader.SERIALIZED);

        RawPropertyData rpd;
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
            rpd = new RawPropertyData(curComponent, curType,
                    spd.getProperties());
        } else {
            if (rpdMap.get(curComponent) != null) {
                throw new ConfigLoaderException("duplicate definition for "
                        + curComponent);
            }
            rpd = new RawPropertyData(curComponent, curType, null);
        }

        //
        // Set the lease time.
        rpd.setExportable(exportable);
        rpd.setImportable(importable);
        rpd.setLeaseTime(leaseTime);
        String entriesName = entriesNameNode != null ? entriesNameNode.textValue() : null;
        rpd.setEntriesName(entriesName);
        String serializedForm = serializedFormNode != null ? serializedFormNode.textValue() : null;
        rpd.setSerializedForm(serializedForm);

        ArrayNode properties = (ArrayNode) node.get(ConfigLoader.PROPERTIES);
        for (JsonNode n : properties) {
            ObjectNode propNode = (ObjectNode) n;
            String nodeType = propNode.get(JsonLoader.CONFIG_TYPE).textValue();
            JsonNode name;
            JsonNode value;
            ArrayNode list;
            switch (nodeType) {
                case PROPERTY:
                    name = propNode.get(ConfigLoader.NAME);
                    value = propNode.get(ConfigLoader.VALUE);
                    if (name == null || value == null) {
                        throw new ConfigLoaderException("Property element must have "
                                + "'name' and 'value' attributes, found " + node.toString());
                    }
                    rpd.add(name.textValue(),value.textValue());
                    break;
                case PROPERTYLIST:
                    name = propNode.get(ConfigLoader.NAME);
                    list = (ArrayNode) propNode.get(JsonLoader.LIST);
                    ArrayList valueList = new ArrayList();
                    for (JsonNode element : list) {
                        ObjectNode listNode = (ObjectNode) element;
                        String elementType = listNode.get(JsonLoader.CONFIG_TYPE).textValue();
                        String member = listNode.get(JsonLoader.MEMBER).textValue();
                        if (elementType != null) {
                            switch (elementType) {
                                case ConfigLoader.ITEM:
                                    valueList.add(member);
                                    break;
                                case ConfigLoader.TYPE:
                                    try {
                                        valueList.add(Class.forName(member));
                                    } catch (ClassNotFoundException cnfe) {
                                        throw new ConfigLoaderException("Unable to find class "
                                                + member + " in component " + curComponent + ", propertylist " + name);
                                    }
                                    break;
                                default:
                                    throw new ConfigLoaderException("Unknown node in component " + curComponent + ", propertylist " + name + ", node = " + node.toString());
                            }
                        } else {
                            throw new ConfigLoaderException("Unknown node in component " + curComponent + ", propertylist " + name + ", node = " + node.toString());
                        }
                    }
                    rpd.add(name.textValue(),valueList);
                    break;
                case PROPERTYMAP:
                    name = propNode.get(ConfigLoader.NAME);
                    list = (ArrayNode) propNode.get(JsonLoader.LIST);
                    Map<String,String> map = new HashMap<>();

                    for (JsonNode element : list) {
                        ObjectNode listNode = (ObjectNode) element;
                        JsonNode key = listNode.get(ConfigLoader.KEY);
                        value = listNode.get(ConfigLoader.VALUE);
                        if (key == null || value == null) {
                            throw new ConfigLoaderException("Entry element must have "
                                    + "'key' and 'value' attributes, found " + node.toString());
                        } else if (map.containsKey(key.textValue())) {
                            throw new ConfigLoaderException("Repeated entry in map, key = " + key + " already exists in component " + curComponent);
                        }
                        map.put(key.textValue(), value.textValue());
                    }
                    rpd.add(name.textValue(),map);
                    break;
                default:
                    throw new ConfigLoaderException("Unknown node in component " + curComponent + ", node = " + node.toString());
            }
        }
        rpdMap.put(rpd.getName(),rpd);
    }

    protected void parseFile(ObjectNode node) {
        String name = node.get(ConfigLoader.NAME).textValue();
        String value = node.get(ConfigLoader.VALUE).textValue();
        if (name == null || value == null) {
            throw new ConfigLoaderException("File element must have "
                    + "'name' and 'value' attributes, found " + node.toString());
        }
        try {
            URL newURL = ConfigurationManager.class.getResource(value);
            if (newURL == null) {
                newURL = (new File(value)).toURI().toURL();
            }
            parent.addURL(newURL);
        } catch (MalformedURLException ex) {
            throw new ConfigLoaderException(ex, "Incorrectly formatted file element " + name + " with value " + value);
        }
    }

    protected void parseSerializedObject(ObjectNode node) {
        String name = node.get(ConfigLoader.NAME).textValue();
        String type = node.get(ConfigLoader.TYPE).textValue();
        String location = node.get(ConfigLoader.LOCATION).textValue();
        if ((name == null) || (type == null) || (location == null)) {
            throw new ConfigLoaderException("Serialized element must have 'name', 'type' and 'location' elements, found " + node.toString());
        }
        serializedObjects.put(name, new SerializedObject(name, location, type));
    }
}
