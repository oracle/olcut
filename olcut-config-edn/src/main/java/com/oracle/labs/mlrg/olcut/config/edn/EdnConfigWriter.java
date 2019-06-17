package com.oracle.labs.mlrg.olcut.config.edn;

import com.oracle.labs.mlrg.olcut.config.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.ConfigWriter;
import com.oracle.labs.mlrg.olcut.config.ConfigWriterException;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.Property;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import us.bpsm.edn.EdnException;
import us.bpsm.edn.Keyword;
import us.bpsm.edn.Symbol;
import us.bpsm.edn.printer.Printer;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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
        this.printer = new OlcutEdnPrinter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
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

    private Object writeProperty(Property p) throws ConfigWriterException {
        Object res;
        if(p instanceof MapProperty) {
            // map configurable field
            Map<Keyword, String> mRes = new HashMap<>();
            for(Map.Entry<String, SimpleProperty> e: ((MapProperty) p).getMap().entrySet()) {
                if(e.getKey()==null) {
                    throw new ConfigWriterException(new IllegalArgumentException("Can't write a map with null keys" + p.toString()));
                }
                if(e.getValue()==null) {
                    throw new ConfigWriterException(new IllegalArgumentException("Can't write a map with null values: " + p.toString()));
                }
                mRes.put(Keyword.newKeyword(e.getKey()), e.getValue().getValue());
            }
            res = mRes;
        } else if(p instanceof ListProperty) {
            // list configurable field
            List<Object> lRes = new ArrayList<>();
            for (SimpleProperty s : ((ListProperty)p).getSimpleList()) {
                if(s==null) {
                    throw new ConfigWriterException(new IllegalArgumentException("Can't write a list with null values: " + p.toString()));
                }
                lRes.add(s.getValue());
            }
            for (Class<?> c : ((ListProperty) p).getClassList()) {
                if(c==null) {
                    throw new ConfigWriterException(new IllegalArgumentException("Can't write a list with null values: " + p.toString()));
                }
                lRes.add(cnMapper.write(c.getCanonicalName()));
            }
            res = lRes;
        } else if(p instanceof SimpleProperty) {
            res = ((SimpleProperty) p).getValue();
        } else {
            throw new ConfigWriterException(new IllegalArgumentException("Unexpected type for property value " + p.getClass().toString() + " with value " + p));
        }
        return res;
    }

    @Override
    public void writeComponent(Map<String, String> attributes, Map<String, Property> properties) {
        List<Object> compList = new LinkedList<>(Arrays.asList(
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
        for(Map.Entry<String, Property> e: properties.entrySet()) {
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
