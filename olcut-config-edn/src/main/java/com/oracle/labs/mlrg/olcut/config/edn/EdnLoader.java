package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.RawPropertyData;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.URLLoader;
import us.bpsm.edn.Keyword;
import us.bpsm.edn.Symbol;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EdnLoader implements ConfigLoader {

    private static final Logger logger = Logger.getLogger(EdnLoader.class.getName());
    private ClassnameMapper cnMapper;

    private static <T> List<T> rest(List<T> l) {
        return l.subList(1, l.size());
    }

    private static String checkSymbol(Object o) throws ConfigLoaderException {
        if(o instanceof Symbol) {
            return ((Symbol) o).getName();
        } else {
            throw new ConfigLoaderException("Expected Symbol but found " + o.getClass() + " with value " + o.toString());
        }
    }

    private static String checkKeyword(Object o) throws ConfigLoaderException {
        if(o instanceof Keyword) {
            return ((Keyword) o).getName();
        } else {
            throw new ConfigLoaderException("Expected Keyword but found " + o.getClass() + " with value " + o.toString());
        }
    }

    private String checkClassList(Object os) throws ConfigLoaderException {
        if(os instanceof List<?>) {
            return cnMapper.read((List<Symbol>) os);
            /*
            StringBuilder sb = new StringBuilder();
            Iterator<?> iter = ((List<?>) os).iterator();
            String sym = checkSymbol(iter.next());
            sb.append(expandPrefix.getOrDefault(sym, sym));
            while (iter.hasNext()) {
                sb.append(".");
                sb.append(checkSymbol(iter.next()));

            }
            return sb.toString();
            */
        } else {
            throw new ConfigLoaderException("Expected class but found " + os.getClass() + " with value " + os.toString());
        }
    }

    private static String checkString(Object o) throws ConfigLoaderException {
        if(o instanceof String) {
            return (String) o;
        } else {
            throw new ConfigLoaderException("Expected Symbol but found " + o.getClass() + " with value " + o.toString());
        }
    }

    private static boolean checkBoolean(Object o) throws ConfigLoaderException {
        if(o instanceof Boolean) {
            return (boolean) o;
        } else if(o instanceof String) {
                return Boolean.valueOf((String) o);
        } else {
            throw new ConfigLoaderException("Expected boolean or boolean string but found " + o.getClass() + " with value " + o.toString());
        }
    }

    private static long checkLong(Object o) throws ConfigLoaderException {
        if(o instanceof Long) {
            return (long) o;
        } else if(o instanceof String) {
            try {
                return Long.valueOf((String) o);
            } catch (NumberFormatException e) {
                throw new ConfigLoaderException("Expected long or long string but found " + o.getClass() + " with value " + o.toString());
            }
        } else {
            throw new ConfigLoaderException("Expected long or long string but found " + o.getClass() + " with value " + o.toString());
        }
    }


    private final URLLoader parent;
    private final Map<String, RawPropertyData> rpdMap;
    private final Map<String, RawPropertyData> existingRPD;
    private final Map<String, SerializedObject> serializedObjects;
    private final GlobalProperties globalProperties;

    public EdnLoader(URLLoader parent, Map<String, RawPropertyData> rpdMap, Map<String, RawPropertyData> existingRPD,
                     Map<String, SerializedObject> serializedObjects, GlobalProperties globalProperties) {
        this.parent = parent;
        this.rpdMap = rpdMap;
        this.existingRPD = existingRPD;
        this.serializedObjects = serializedObjects;
        this.globalProperties = globalProperties;
        cnMapper = new ClassnameMapper();
    }

    @Override
    public void load(URL url) throws ConfigLoaderException, IOException {
        try(Parseable pbr = Parsers.newParseable(new BufferedReader(new InputStreamReader(url.openStream())))) {
            parseEdn(pbr);
        }
    }

    @Override
    public String getExtension() {
        return "edn";
    }

    private void parseEdn(Parseable in) {
        Parser p = Parsers.newParser(Parsers.defaultConfiguration());
        Object parseValue = p.nextValue(in);
        if (parseValue instanceof List<?>) {
            List<?> config = (List<?>) parseValue;
            if(checkSymbol(config.get(0)).equals(ConfigLoader.CONFIG)) {
                for(Object configObj : rest(config)) {
                    if(configObj instanceof List<?>) {
                        List<?> configListItem = (List<?>) configObj;
                        switch (checkSymbol(configListItem.get(0))) {
                            case PROPERTIES:
                                parseGlobalProperties(rest(configListItem));
                                break;
                            case PROPERTY:
                                parseGlobalProperty(rest(configListItem));
                                break;
                            case COMPONENT:
                                parseComponent(rest(configListItem));
                                break;
                            case FILE:
                                parseFile(rest(configListItem));
                                break;
                            case SERIALIZED:
                                parseSerializedObject(rest(configListItem));
                                break;
                        }
                    }
                }
            } else {
                throw new ConfigLoaderException("Did not start with " + ConfigLoader.CONFIG + " list");
            }
        } else {
            throw new ConfigLoaderException("Did not start with " + ConfigLoader.CONFIG + " list");
        }
    }

    private void parseGlobalProperties(List<?> propertiesListItem) {
        if(propertiesListItem.size() % 2 != 0) {
            throw new ConfigLoaderException("Properties element must have an even number of arguments, found " + propertiesListItem.toString());
        }
        for(int i=0; i<propertiesListItem.size(); i++) {
            String name = checkSymbol(propertiesListItem.get(i));
            String value = checkSymbol(propertiesListItem.get(i+1));
            globalProperties.setValue(name, value);
        }
    }

    private void parseGlobalProperty(List<?> propertyListItem) {
        if (propertyListItem.size() != 2) {
            throw new ConfigLoaderException("Property element must have both name and attribute, found " + propertyListItem.toString());
        }
        String name = checkSymbol(propertyListItem.get(0));
        String value = checkString(propertyListItem.get(1));
        globalProperties.setValue(name, value);
    }

    private void parseFile(List<?> fileListItem) {
        if(fileListItem.size() != 2) {
            throw new ConfigLoaderException("File element must have both name and file path string, found " + fileListItem.toString());
        }
        String name = checkSymbol(fileListItem.get(0));
        String path = checkString(fileListItem.get(1));
        try {
            URL newURL = ConfigurationManager.class.getResource(path);
            if(newURL == null) {
                newURL = (new File(path)).toURI().toURL();
            }
            parent.addURL(newURL);
        } catch (MalformedURLException ex) {
            throw new ConfigLoaderException(ex, "Incorrectly formatted file element " + name + " with value " + path);
        }
    }

    private void parseComponent(List<?> componentListItem) {
        if(componentListItem.size() < 2) {
            throw new ConfigLoaderException("Component element must have name and type, found " + componentListItem.toString());
        }
        String name = checkSymbol(componentListItem.get(0));
        String type = checkClassList(componentListItem.get(1));
        int propsStart = 2;
        RawPropertyData rpd = new RawPropertyData(name, type, null);

        if(componentListItem.get(2) instanceof Map<?, ?>) {
            boolean importable = false;
            boolean exportable = false;
            String override = null;
            long leaseTime = 0L;
            String entriesName = null;
            String serializedForm = null;

            Map<?, ?> modMap = (Map<?, ?>) componentListItem.get(2);
            if(modMap.containsKey(ConfigLoader.INHERIT)) {
                override = checkString(modMap.get(ConfigLoader.INHERIT));
            }
            if(modMap.containsKey(ConfigLoader.IMPORT)) {
                importable = checkBoolean(modMap.get(ConfigLoader.IMPORT));
            }
            if(modMap.containsKey(ConfigLoader.EXPORT)) {
                exportable = checkBoolean(modMap.get(ConfigLoader.EXPORT));
            }
            if(modMap.containsKey(ConfigLoader.LEASETIME)) {
                if(!exportable) {
                    throw new ConfigLoaderException("lease timeout " + leaseTime +
                            " specified for component that does not have export set, at " + modMap.toString());
                }
                leaseTime = checkLong(modMap.get(ConfigLoader.LEASETIME));
            }
            if(modMap.containsKey(ConfigLoader.ENTRIES)) {
                entriesName = modMap.get(ConfigLoader.ENTRIES).toString();
            }
            if(modMap.containsKey(ConfigLoader.SERIALIZED)) {
                serializedForm = modMap.get(ConfigLoader.SERIALIZED).toString();
            }

            if(override != null) {
                RawPropertyData spd = rpdMap.get(override);
                if (spd == null) {
                    spd = existingRPD.get(override);
                    if (spd == null) {
                        throw new ConfigLoaderException("Override for undefined component: "
                                + override + ", with name " + name);
                    }
                }
                if (!type.equals(spd.getClassName())) {
                    logger.log(Level.FINE, String.format("Overriding component %s with component %s, new type is %s overridden type was %s",
                            spd.getName(), name , type, spd.getClassName()));
                }
                type = spd.getClassName();
                rpd = new RawPropertyData(name, type,
                        spd.getProperties());
            } else {
                if (rpdMap.get(name) != null) {
                    throw new ConfigLoaderException("duplicate definition for "
                            + name);
                }
                rpd = new RawPropertyData(name, type, null);
            }
            rpd.setExportable(exportable); //todo check this inspection
            rpd.setExportable(importable);
            rpd.setLeaseTime(leaseTime);
            rpd.setEntriesName(entriesName);
            rpd.setSerializedForm(serializedForm);
            propsStart = 3;
        }
        List<?> props = componentListItem.subList(propsStart, componentListItem.size());
        if(props.size() % 2 != 0) {
            throw new ConfigLoaderException("Component element should have an even number of property key values pairs, found " + props.toString());
        }
        for(int i = 0; i < props.size(); i=i+2) {
            int keyIdx = i;
            int valIdx = i+1;
            String key = checkKeyword(props.get(keyIdx));
            Object valObj = props.get(valIdx);
            if(valObj instanceof Map<?, ?>) {
                Map<String, String> map = new HashMap<>();
                for(Map.Entry<?, ?> ent: ((Map<?, ?>) valObj).entrySet()) {
                    map.put(checkKeyword(ent.getKey()), ent.getValue().toString());
                }
                rpd.add(key, map);
            } else if(valObj instanceof List<?>) {
                List valueList = new ArrayList();
                for(Object itm: (List<?>) valObj) {
                    if(itm instanceof List<?>) {
                        String member = checkClassList(itm);
                        valueList.add(member);
                        try {
                            Class<?> valCls = Class.forName(member);
                        } catch (ClassNotFoundException e) {
                            throw new ConfigLoaderException("Unable to find class "
                                    + member + ", propertylist " + itm);
                        }
                    } else if(itm instanceof Symbol
                            || itm instanceof String
                            || itm instanceof Integer
                            || itm instanceof Long
                            || itm instanceof Float
                            || itm instanceof Double
                            || itm instanceof Boolean) {
                        valueList.add(itm.toString());
                    } else {
                        throw new ConfigLoaderException("Unexpected type for property value " + valObj.getClass().toString() + " with value " + valObj);
                    }
                }
                rpd.add(key, valueList);
            } else if(valObj instanceof Symbol
                    || valObj instanceof String
                    || valObj instanceof Integer
                    || valObj instanceof Long
                    || valObj instanceof Float
                    || valObj instanceof Double
                    || valObj instanceof Boolean) {
                rpd.add(key, valObj.toString());
            } else {
                throw new ConfigLoaderException("Unexpected type for property value " + valObj.getClass().toString() + " with value " + valObj);
            }
        }
        rpdMap.put(rpd.getName(), rpd);
    }

    private void parseSerializedObject(List<?> serializedListItem) {
        if(serializedListItem.size() != 3) {
            throw new ConfigLoaderException("Serialized element must have name, location, and type elements, found " + serializedListItem.toString());
        }
        String name = checkSymbol(serializedListItem.get(0));
        String location = checkString(serializedListItem.get(1));
        String type = checkClassList(serializedListItem.get(2));
        serializedObjects.put(name, new SerializedObject(name, location, type));
    }
}
