package com.sun.labs.util.props;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The types of fields that can be annotated by the {@Config} annotation.
 */
public enum FieldType {
    
    BOOLEAN(boolean.class),
    INTEGER(int.class),
    INTEGER_ARRAY(int[].class),
    LONG(long.class),
    LONG_ARRAY(long[].class),
    FLOAT(float.class),
    FLOAT_ARRAY(float[].class),
    DOUBLE(double.class),
    DOUBLE_ARRAY(double[].class),
    STRING(String.class),
    STRING_ARRAY(String[].class),
    COMPONENT(Component.class),
    COMPONENT_ARRAY(Component[].class), 
    FILE(File.class),
    PATH(Path.class),
    ENUM(Enum.class),
    ENUM_SET(EnumSet.class);
    
    private final Class<?> type;
    
    private final static Map<Class<?>,FieldType> m = new HashMap<>();

    private FieldType(Class<?> type) {
        this.type = type;
    }
    
    static {
        for(FieldType ft : FieldType.values()) {
            m.put(ft.type, ft);
        }
    }

    public static FieldType getFieldType(Field f) {
        return m.get(f.getType());
    }
    
}
