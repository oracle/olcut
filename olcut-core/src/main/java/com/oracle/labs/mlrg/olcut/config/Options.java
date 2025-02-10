/*
 * Copyright (c) 2004, 2025, Oracle and/or its affiliates.
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

import com.oracle.labs.mlrg.olcut.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A tag interface which contains fields annotated with {@link Option}, and other
 * fields which subclass {@link Options}. See the README for usage.
 * <p>
 * Implement the {@link Options#getOptionsDescription} method to insert a description string
 * into the generated Options usage string.
 * <p>
 * Any other fields or methods are ignored by the
 * command line arguments processing system.
 * <p>
 * Options implementations should form a tree, with no duplication of classes.
 * Implementations which are not a tree or have duplicate classes will fail
 * the validation checks in {@link ConfigurationManager#validateOptions}.
 */
public interface Options {
    public static final List<String> header = Collections.unmodifiableList(Arrays.asList("Char","Long Name","Type","Default","Usage"));

    /**
     * Gets a possibly multi line description of this Options subclass.
     * <p>
     * Default implementation returns the empty string.
     * @return A description string.
     */
    default public String getOptionsDescription() {
        return "";
    }

    /**
     * Constructs a formatted usage string from a table of fields.
     * @param usageList The fields.
     * @return A usage string.
     */
    public static String formatUsage(List<List<String>> usageList) {
        int[] maxWidth = new int[5];

        for (List<String> a : usageList) {
            if (a.size() == 5) {
                if (maxWidth[0] < a.get(0).length()) {
                    maxWidth[0] = a.get(0).length();
                }
                if (maxWidth[1] < a.get(1).length()) {
                    maxWidth[1] = a.get(1).length();
                }
                if (maxWidth[2] < a.get(2).length()) {
                    maxWidth[2] = a.get(2).length();
                }
                if (maxWidth[3] < a.get(3).length()) {
                    maxWidth[3] = a.get(3).length();
                }
            }
        }

        String formatString = "%"+maxWidth[0]+"s %-"+maxWidth[1]+"s %-"+maxWidth[2]+"s %-"+maxWidth[3]+"s %s\n";
        StringBuilder builder = new StringBuilder();

        for (List<String> a : usageList) {
            if (a.size() == 5) {
                builder.append(String.format(formatString, a.get(0), a.get(1), a.get(2), a.get(3), a.get(4)));
            } else if (a.size() == 2) {
                builder.append(a.get(0));
                builder.append(a.get(1));
                builder.append("\n");
            } else {
                // Must be Option class name
                builder.append("\n");
                builder.append(a.get(0));
                builder.append("\n\n");
            }
        }

        return builder.toString();
    }

    /**
     * Constructs the usage string from the supplied Options subclass.
     * @param options The options to construct a usage for.
     * @return The usage string.
     */
    public static List<List<String>> getUsage(Class<? extends Options> options) {
        ArrayList<List<String>> list = new ArrayList<>();
        ArrayList<List<String>> optionsList = new ArrayList<>();
        Set<Field> fields = getOptionFields(options);
        if (fields.isEmpty()) {
            return list;
        } else {
            list.add(new ArrayList<>(Collections.singletonList(options.getSimpleName())));

            try {
                Options opt = options.getDeclaredConstructor().newInstance();
                String optUsage = opt.getOptionsDescription();
                if (!optUsage.isEmpty()) {
                    list.add(Arrays.asList("Description: ", optUsage));
                }
                list.add(header);
                for (Field f : fields) {
                    Option option = f.getAnnotation(Option.class);
                    optionsList.add(Options.getOptionUsage(option,f,opt));
                }

                optionsList.sort((List<String> a, List<String> b) -> {
                    if (a.get(0).charAt(0) == b.get(0).charAt(0)) {
                        return a.get(1).compareTo(b.get(1));
                    } else {
                        if (a.get(0).charAt(0) == Option.SPACE_CHAR) {
                            return +1;
                        } else if (b.get(0).charAt(0) == Option.SPACE_CHAR) {
                            return -1;
                        } else {
                            return a.get(0).compareTo(b.get(0));
                        }
                    }
                });
                list.addAll(optionsList);

                return list;
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new ArgumentException(e,"Could not instantiate Options class " + options.getName() + ", it has no default constructor.");
            } catch (IllegalAccessException e) {
                if (!Modifier.isPublic(options.getModifiers())) {
                    throw new ArgumentException(e,"Could not instantiate Options class " + options.getName() + ", it must be public.");
                }
                try {
                    Constructor<?> constructor = options.getDeclaredConstructor();
                    if (!Modifier.isPublic(constructor.getModifiers())) {
                        throw new ArgumentException(e,"Could not instantiate Options class " + options.getName() + ", its default constructor must be public.");
                    }
                } catch (NoSuchMethodException nsme) {
                    throw new ArgumentException(e,"Could not instantiate Options class " + options.getName() + ", it has no default constructor.");
                }
                throw new ArgumentException(e,"Could not instantiate Options class " + options.getName());
            }

        }
    }

    /**
     * Returns a String representing the Enum constants from this class surrounded by '{', '}'
     * and separated by a comma and a space.
     * @param enumClazz The enum class to represent.
     * @return A String containing all the enum constants.
     */
    public static String getEnumConstantString(Class<? extends Enum<?>> enumClazz) {
        Enum<?>[] constants = enumClazz.getEnumConstants();
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (Enum<?> o : constants) {
            sb.append(o.name());
            sb.append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "}");
        return sb.toString();
    }

    public static String generateTypeDescription(Field f) {
        Class<?> clazz = f.getType();
        if (clazz.isEnum()) {
            @SuppressWarnings("unchecked") //guarded by isEnum check
            Class<? extends Enum<?>> enumClazz = (Class<? extends Enum<?>>) clazz;
            return "enum - " + getEnumConstantString(enumClazz);
        } else if (clazz == EnumSet.class) {
            Type type = f.getGenericType();
            if (type instanceof ParameterizedType typeName) {
                // Should only have a single type parameter
                Type enumType = typeName.getActualTypeArguments()[0];
                try {
                    @SuppressWarnings("unchecked") // type parameter to an enumset must be an enum
                    Class<? extends Enum<?>> enumClazz = (Class<? extends Enum<?>>) Class.forName(enumType.getTypeName());
                    return "EnumSet - " + getEnumConstantString(enumClazz);
                } catch (ClassNotFoundException e) {
                    Logger.getLogger(Options.class.getName()).warning("Failed to load enum class '" + enumType.getTypeName() + "'");
                    return typeName.getTypeName();
                }
            } else {
                return f.getGenericType().getTypeName();
            }
        } else {
            return f.getGenericType().getTypeName();
        }
    }

    /**
     * Gets the fields for this option's usage string.
     * @param option The option annotation.
     * @param f The annotated field.
     * @param obj The parent options object, used to access the default value for this field.
     * @return The fields for the usage string.
     */
    public static ArrayList<String> getOptionUsage(Option option, Field f, Options obj) {
        String typeString = generateTypeDescription(f);
        return getOptionUsage(option,f,obj,typeString);
    }

    /**
     * Gets the usage for one of the default options (which don't have a parent options object).
     * @param option The option annotation.
     * @param type The type of the option.
     * @return The fields for the usage string.
     */
    public static ArrayList<String> getOptionUsage(Option option, String type) {
        ArrayList<String> output = new ArrayList<>();
        if (option.charName() != Option.EMPTY_CHAR) {
            output.add(""+option.charName());
        } else {
            output.add(""+Option.SPACE_CHAR);
        }
        output.add(option.longName());
        output.add(type);
        output.add("");
        output.add(option.usage());
        return output;
    }

    /**
     * Gets the usage fields for the supplied option.
     * @param option The option annotation.
     * @param f The field the annotation is attached to.
     * @param obj The parent options object, used to access the default value for this field.
     * @param type The type string used in the usage (may be the enum constants, or a short type descriptor).
     * @return The fields for the usage string.
     */
    public static ArrayList<String> getOptionUsage(Option option, Field f, Options obj, String type) {
        ArrayList<String> output = new ArrayList<>();
        if (option.charName() != Option.EMPTY_CHAR) {
            output.add(""+option.charName());
        } else {
            output.add(""+Option.SPACE_CHAR);
        }
        output.add(option.longName());
        output.add(type);
        Object extractedField;
        try {
            f.setAccessible(true);
            Object fieldVal = f.get(obj);
            f.setAccessible(false);
            extractedField = fieldVal;
        } catch (IllegalAccessException e) {
            Logger.getLogger(Options.class.getName()).fine("Failed to read default value from field " + option.longName());
            extractedField = null;
        }
        String defaultVal = extractedField == null ? "" : extractedField.toString();
        output.add(defaultVal);
        output.add(option.usage());
        return output;
    }

    /**
     * Gets all of the fields annotated with {@link Option} associated with a
     * class by walking up the class tree. Handles super classes, as well
     * as interfaces.
     *
     * @param options the class who's fields we wish to walk.
     * @return all of the fields annotated with {@link Option}.
     */
    public static Set<Field> getOptionFields(Class<? extends Options> options) {
        Set<Field> ret = new HashSet<>();
        Queue<Class<?>> cq = new ArrayDeque<>();
        cq.add(options);
        while (!cq.isEmpty()) {
            Class<?> curr = cq.remove();
            for (Field f : curr.getDeclaredFields()) {
                if (f.getAnnotation(Option.class) != null) {
                    ret.add(f);
                }
            }
            for (Field f : curr.getFields()) {
                if (f.getAnnotation(Option.class) != null) {
                    ret.add(f);
                }
            }
            Class<?> sc = curr.getSuperclass();
            if (sc != null) {
                cq.add(sc);
            }
            cq.addAll(Arrays.asList(curr.getInterfaces()));
        }
        ret.removeIf(f -> Modifier.isStatic(f.getModifiers()));
        return ret;
    }

    /**
     * Gets all of the fields which subclass {@link Options} from a class by
     * walking up the class tree. Handles super classes, as well
     * as interfaces.
     *
     * @param options the class who's fields we wish to walk.
     * @return all of the fields which subclass {@link Options}.
     */
    public static Set<Field> getOptions(Class<? extends Options> options) {
        Set<Field> ret = new HashSet<>();
        Queue<Class<?>> cq = new ArrayDeque<>();
        cq.add(options);
        while (!cq.isEmpty()) {
            Class<?> curr = cq.remove();
            for (Field f : curr.getDeclaredFields()) {
                if (Options.class.isAssignableFrom(f.getType())) {
                    ret.add(f);
                }
            }
            for (Field f : curr.getFields()) {
                if (Options.class.isAssignableFrom(f.getType())) {
                    ret.add(f);
                }
            }
            Class<?> sc = curr.getSuperclass();
            if (sc != null) {
                cq.add(sc);
            }
            cq.addAll(Arrays.asList(curr.getInterfaces()));
        }
        ret.removeIf(f -> Modifier.isStatic(f.getModifiers()));
        return ret;
    }

    /**
     * Gets all of the fields which subclass {@link Options} by walking up the
     * class tree. Handles super classes, as well as interfaces.
     *
     * @param options the class who's fields we wish to walk.
     * @return all of the fields which subclass Options.
     */
    @SuppressWarnings("unchecked")
    public static Set<Class<? extends Options>> getAllOptions(Class<? extends Options> options) {
        // The Pair is (classname,fieldname)
        Map<Class<? extends Options>, Pair<String, String>> ret = new LinkedHashMap<>();
        Map<Pair<String, String>, Class<? extends Options>> tempSet = new HashMap<>();
        ret.put(options, new Pair<>("root", "root"));
        Queue<Class<?>> cq = new ArrayDeque<>();
        cq.add(options);
        while (!cq.isEmpty()) {
            Class<?> curr = cq.remove();
            String currName = curr.getName();
            for (Field f : curr.getDeclaredFields()) {
                if (Options.class.isAssignableFrom(f.getType()) && !Modifier.isStatic(f.getModifiers())) {
                    Class<? extends Options> nextOptions = (Class<? extends Options>) f.getType();
                    // Add to the processing queue, via a map to make sure we don't double count fields.
                    tempSet.put(new Pair<>(currName, f.getName()), nextOptions);
                }
            }
            for (Field f : curr.getFields()) {
                if (Options.class.isAssignableFrom(f.getType()) && !Modifier.isStatic(f.getModifiers())) {
                    Class<? extends Options> nextOptions = (Class<? extends Options>) f.getType();
                    // Add to the processing queue, via a map to make sure we don't double count fields.
                    tempSet.put(new Pair<>(currName, f.getName()), nextOptions);
                }
            }
            Class<?> sc = curr.getSuperclass();
            if (sc != null) {
                cq.add(sc);
            }
            cq.addAll(Arrays.asList(curr.getInterfaces()));

            // Append the processing queue to the returned set, validating that each one is unique.
            for (Map.Entry<Pair<String, String>, Class<? extends Options>> e : tempSet.entrySet()) {
                if (ret.containsKey(e.getValue())) {
                    Pair<String, String> otherOccurrence = ret.get(e.getValue());
                    String firstOccurrence = otherOccurrence.getA() + "." + otherOccurrence.getB();
                    String thisOccurrence = e.getKey().getA() + "." + e.getKey().getB();
                    throw new ArgumentException(firstOccurrence, thisOccurrence, "There are two instances of " + e.getValue().getName() + " in this Options tree.");
                } else {
                    ret.put(e.getValue(), e.getKey());
                    cq.add(e.getValue());
                }
            }

            // Flush the temporary set.
            tempSet.clear();
        }
        return new LinkedHashSet<>(ret.keySet());
    }
}
