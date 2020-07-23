/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The types of fields that can be annotated by the {@link Config} annotation.
 */
public enum FieldType {

    //Primitives
    BOOLEAN(boolean.class, Boolean.class),
    BYTE(byte.class, Byte.class),
    CHAR(char.class, Character.class),
    SHORT(short.class, Short.class),
    INTEGER(int.class, Integer.class),
    LONG(long.class, Long.class),
    FLOAT(float.class, Float.class),
    DOUBLE(double.class, Double.class),
    STRING(String.class),
    //Primitive array types
    BYTE_ARRAY(byte[].class),
    CHAR_ARRAY(char[].class),
    SHORT_ARRAY(short[].class),
    INTEGER_ARRAY(int[].class),
    LONG_ARRAY(long[].class),
    FLOAT_ARRAY(float[].class),
    DOUBLE_ARRAY(double[].class),
    //Configurable classes
    CONFIGURABLE(Configurable.class),
    //Object array types
    STRING_ARRAY(String[].class),
    CONFIGURABLE_ARRAY(Configurable[].class),
    //Generic types - requires genericType argument to be set
    LIST(List.class),
    ENUM_SET(EnumSet.class),
    SET(Set.class),
    MAP(Map.class), //Map<String,T>
    //Misc types
    ATOMIC_INTEGER(AtomicInteger.class),
    ATOMIC_LONG(AtomicLong.class),
    FILE(File.class),
    PATH(Path.class),
    URL(URL.class),
    DATE_TIME(OffsetDateTime.class),
    DATE(LocalDate.class),
    TIME(OffsetTime.class),
    ENUM(Enum.class),
    @Deprecated
    RANDOM(Random.class);

    private static final Class<?> configurableClass = Configurable.class;
    private static final Class<?> configurableArrayClass = Configurable[].class;
    private static final Class<?> enumClass = Enum.class;

    private final List<Class<?>> types;
    
    private final static Map<Class<?>,FieldType> m = new HashMap<>();
    
    public final static EnumSet<FieldType> arrayTypes = EnumSet.of(BYTE_ARRAY, CHAR_ARRAY, SHORT_ARRAY,
                                                                  INTEGER_ARRAY, LONG_ARRAY,
                                                                  FLOAT_ARRAY, DOUBLE_ARRAY, STRING_ARRAY,
                                                                  CONFIGURABLE_ARRAY);

    public final static EnumSet<FieldType> listTypes = EnumSet.of(LIST,SET,ENUM_SET);

    public final static EnumSet<FieldType> simpleTypes = EnumSet.of(BOOLEAN, BYTE, CHAR, SHORT, INTEGER, LONG, FLOAT, DOUBLE, STRING,
                                                                    CONFIGURABLE, ATOMIC_INTEGER,
                                                                    ATOMIC_LONG, FILE, PATH, URL, RANDOM, DATE_TIME, DATE, TIME, ENUM);

    public final static EnumSet<FieldType> mapTypes = EnumSet.of(MAP);

    // Used by the options processing system.
    public final static EnumSet<FieldType> configurableTypes = EnumSet.of(CONFIGURABLE,CONFIGURABLE_ARRAY);

    FieldType(Class<?>... types) {
        this.types = Collections.unmodifiableList(Arrays.asList(types));
    }

    static {
        for (FieldType ft : FieldType.values()) {
            for (Class<?> type : ft.types) {
                m.put(type, ft);
            }
        }
    }

    public static FieldType getFieldType(Class<?> clazz) {
        if (configurableClass.isAssignableFrom(clazz)) {
            return m.get(configurableClass);
        } else if (configurableArrayClass.isAssignableFrom(clazz)) {
            return m.get(configurableArrayClass);
        } else if (enumClass.isAssignableFrom(clazz)) {
            return m.get(enumClass);
        } else {
            return m.get(clazz);
        }
    }

    public static FieldType getFieldType(Field f) {
        Class<?> fieldClass = f.getType();
        return getFieldType(fieldClass);
    }

    public List<Class<?>> getTypes() {
        return types;
    }

    public static boolean isBoolean(FieldType ft) {
        return ft.equals(FieldType.BOOLEAN);
    }
}
