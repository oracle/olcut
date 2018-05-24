package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import us.bpsm.edn.EdnException;
import us.bpsm.edn.EdnIOException;
import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Keyword;
import us.bpsm.edn.Symbol;
import us.bpsm.edn.printer.Printer;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EdnConfigWriter implements ConfigWriter {

    private Printer printer;
    private List<Object> struct;
    private ClassnameMapper cnMapper;
    private static final Set<String> COMPONENT_MODIFIERS = new HashSet<>(Arrays.asList(ConfigLoader.IMPORT, ConfigLoader.EXPORT, ConfigLoader.ENTRIES, ConfigLoader.LEASETIME, ConfigLoader.SERIALIZED));

    public EdnConfigWriter(OutputStream os) {
        this.printer = new OlcutEdnPrinter(new PrintStream(os));
        this.struct = new LinkedList<>();
        cnMapper = new ClassnameMapper();
    }

    @Override
    public void writeStartDocument() throws ConfigWriterException {
        struct.add(Symbol.newSymbol("config"));
    }

    @Override
    public void writeEndDocument() throws ConfigWriterException {
        try {
            printer.printValue(struct);
        } catch (EdnException  e) {
            throw new ConfigWriterException(e);
        }
    }

    @Override
    public void writeGlobalProperties(Map<String, String> props) throws ConfigWriterException {
        for(Map.Entry<String, String> e: props.entrySet()) {
            if(e.getKey()==null) {
                throw new ConfigWriterException(new IllegalArgumentException("Can't write a map with null keys" + props.toString()));
            }
            if(e.getValue()==null) {
                throw new ConfigWriterException(new IllegalArgumentException("Can't write a map with null values: " + props.toString()));
            }
            struct.add(new LinkedList<>(Arrays.asList(
                    Symbol.newSymbol(ConfigLoader.PROPERTY),
                    Symbol.newSymbol(e.getKey()),
                    e.getValue())));
        }
    }

    @Override
    public void writeSerializedObjects(Map<String, SerializedObject> map) throws ConfigWriterException {
        for(SerializedObject ser: map.values()) {
            struct.add(new LinkedList<>(Arrays.asList(
                    Symbol.newSymbol(ConfigLoader.SERIALIZED),
                    Symbol.newSymbol(ser.getName()),
                    ser.getLocation(),
                    cnMapper.write(ser.getClassName()))));
        }
    }

    @Override
    public void writeStartComponents() throws ConfigWriterException {
    }

    private Object writeProperty(Object p) throws ConfigWriterException {
        Object res;
        if(p instanceof Map<?, ?>) {
            // map configurable field
            Map<Keyword, Object> mRes = new HashMap<>();
            for(Map.Entry<?, ?> e: ((Map<?, ?>) p).entrySet()) {
                if(e.getKey()==null) {
                    throw new ConfigWriterException(new IllegalArgumentException("Can't write a map with null keys" + p.toString()));
                }
                if(e.getValue()==null) {
                    throw new ConfigWriterException(new IllegalArgumentException("Can't write a map with null values: " + p.toString()));
                }
                mRes.put(Keyword.newKeyword(e.getKey().toString()), e.getValue());
            }
            res = mRes;
        } else if(p instanceof List<?>) {
            // list configurable field
            List<Object> lRes = new ArrayList<>();
            for(Object itm: (List<?>) p) {
                if(itm==null) {
                    throw new ConfigWriterException(new IllegalArgumentException("Can't write a list with null values: " + p.toString()));
                }
                if(itm instanceof Class) {
                    lRes.add(cnMapper.write(((Class) itm).getCanonicalName()));
                } else {
                    lRes.add(itm.toString());
                }
            }
            res = lRes;
        } else if(p instanceof Symbol
                || p instanceof String
                || p instanceof Integer
                || p instanceof Long
                || p instanceof Float
                || p instanceof Double
                || p instanceof Boolean
                || p instanceof Character) {
            res = p;
        } else {
            throw new ConfigWriterException(new IllegalArgumentException("Unexpected type for property value " + p.getClass().toString() + " with value " + p));
        }
        return res;
    }

    @Override
    public void writeComponent(Map<String, String> attributes, Map<String, Object> properties) {
        List<Object> compList = new LinkedList<>();
        compList.addAll(Arrays.asList(
                Symbol.newSymbol(ConfigLoader.COMPONENT),
                Symbol.newSymbol(attributes.get(ConfigLoader.NAME)),
                cnMapper.write(attributes.get(ConfigLoader.TYPE))));
        Set<String> intersection = new HashSet<>(attributes.keySet());
        intersection.retainAll(COMPONENT_MODIFIERS);
        if(!intersection.isEmpty()) {
            Map<Keyword, Object> modMap = new HashMap<>();
            for(String k: intersection) {
                modMap.put(Keyword.newKeyword(k), attributes.get(k));
            }
            compList.add(modMap);
        }
        for(Map.Entry<String, Object> e: properties.entrySet()) {
            compList.add(Keyword.newKeyword(e.getKey()));
            compList.add(writeProperty(e.getValue()));
        }
        struct.add(compList);
    }

    @Override
    public void writeEndComponents() throws ConfigWriterException {

    }

    @Override
    public void close() throws ConfigWriterException {
        printer.close();
    }
}
