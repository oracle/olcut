package com.sun.labs.util.props;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The types of fields that can be annotated by the {@Config} annotation.
 */
public enum FieldType {
    
    BOOLEAN(boolean.class),
    INTEGER(int.class, Integer.class),
    ATOMIC_INTEGER(AtomicInteger.class),
    INTEGER_ARRAY(int[].class),
    LONG(long.class, Long.class),
    ATOMIC_LONG(AtomicLong.class),
    LONG_ARRAY(long[].class),
    FLOAT(float.class, Float.class),
    FLOAT_ARRAY(float[].class),
    DOUBLE(double.class, Double.class),
    DOUBLE_ARRAY(double[].class),
    STRING(String.class),
    STRING_ARRAY(String[].class),
    COMPONENT(Component.class),
    COMPONENT_ARRAY(Component[].class), 
    CONFIGURABLE(Configurable.class),
    CONFIGURABLE_ARRAY(Configurable[].class), 
    FILE(File.class),
    PATH(Path.class),
    RANDOM(Random.class),
    MAP(Map.class),
    ENUM(Enum.class),
    ENUM_SET(EnumSet.class);
    
    private final Class<?>[] types;
    
    private final static Map<Class<?>,FieldType> m = new HashMap<>();
    
    public final static EnumSet<FieldType> listTypes = EnumSet.of(STRING_ARRAY, COMPONENT_ARRAY, CONFIGURABLE_ARRAY);

    private FieldType(Class<?>... types) {
        this.types = types;
    }
    
    static {
        for(FieldType ft : FieldType.values()) {
            for(Class<?> type : ft.types) {
                m.put(type, ft);
            }
        }
    }

    public static FieldType getFieldType(Field f) {
        return m.get(f.getType());
    }
    
    public Class<?>[] getTypes() {
        return types;
    }
    
}
