package com.oracle.labs.mlrg.olcut.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A tag interface which contains fields annotated with {@link Option}, and other
 * fields which subclass {@link Options}.
 *
 * Implement the {@link Options#getOptionsDescription} method to insert a description string
 * into the generated Options usage string.
 *
 * Any other fields or methods are ignored by the
 * command line arguments processing system.
 */
public interface Options {
    public static final List<String> header = Collections.unmodifiableList(Arrays.asList("Char","Long Name","Type","Default","Usage"));

    /**
     * Gets a possibly multi line description of this Options subclass.
     *
     * Default implementation returns the empty string.
     * @return A description string.
     */
    default public String getOptionsDescription() {
        return "";
    }

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

    public static List<List<String>> getUsage(Class<? extends Options> options) {
        ArrayList<List<String>> list = new ArrayList<>();
        ArrayList<List<String>> optionsList = new ArrayList<>();
        Set<Field> fields = getOptionFields(options);
        if (fields.size() == 0) {
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
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new ArgumentException(e,"Could not instantiate Options class " + options.getName() +", it has no default constructor.");
            }

        }
    }

    public static String generateTypeDescription(Field f) {
        Class<?> clazz = f.getType();
        if (clazz.isEnum()) {
            Object[] constants = clazz.getEnumConstants();
            StringBuilder sb = new StringBuilder();
            sb.append("enum - {");
            for (Object o : constants) {
                sb.append(((Enum)o).name());
                sb.append(", ");
            }
            sb.replace(sb.length()-2,sb.length(),"}");
            return sb.toString();
        } else {
            return f.getGenericType().getTypeName();
        }
    }

    public static ArrayList<String> getOptionUsage(Option option, Field f, Options obj) {
        String typeString = generateTypeDescription(f);
        return getOptionUsage(option,f,obj,typeString);
    }

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

    public static ArrayList<String> getOptionUsage(Option option, Field f, Options obj, String type) {
        ArrayList<String> output = new ArrayList<>();
        if (option.charName() != Option.EMPTY_CHAR) {
            output.add(""+option.charName());
        } else {
            output.add(""+Option.SPACE_CHAR);
        }
        output.add(option.longName());
        output.add(type);
        Object extractedField = null;
        try {
            boolean accessible = f.isAccessible();
            f.setAccessible(true);
            extractedField = f.get(obj);
            f.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            Logger.getLogger(Options.class.getName()).fine("Failed to read default value from field " + option.longName());
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
        Queue<Class> cq = new ArrayDeque<>();
        cq.add(options);
        while (!cq.isEmpty()) {
            Class curr = cq.remove();
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
            Class sc = curr.getSuperclass();
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
        Queue<Class> cq = new ArrayDeque<>();
        cq.add(options);
        while (!cq.isEmpty()) {
            Class curr = cq.remove();
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
            Class sc = curr.getSuperclass();
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
        Set<Class<? extends Options>> ret = new LinkedHashSet<>();
        Set<Class<? extends Options>> tempSet = new HashSet<>();
        ret.add(options);
        Queue<Class> cq = new ArrayDeque<>();
        cq.add(options);
        while (!cq.isEmpty()) {
            Class curr = cq.remove();
            for (Field f : curr.getDeclaredFields()) {
                if (Options.class.isAssignableFrom(f.getType()) && !Modifier.isStatic(f.getModifiers())) {
                    Class<? extends Options> nextOptions = (Class<? extends Options>)f.getType();
                    ret.add(nextOptions);
                    // Add to the processing queue, via a set to make sure we don't double count fields.
                    tempSet.add(nextOptions);
                }
            }
            for (Field f : curr.getFields()) {
                if (Options.class.isAssignableFrom(f.getType()) && !Modifier.isStatic(f.getModifiers())) {
                    Class<? extends Options> nextOptions = (Class<? extends Options>)f.getType();
                    ret.add(nextOptions);
                    // Add to the processing queue, via a set to make sure we don't double count fields.
                    tempSet.add(nextOptions);
                }
            }
            Class sc = curr.getSuperclass();
            if (sc != null) {
                cq.add(sc);
            }
            cq.addAll(Arrays.asList(curr.getInterfaces()));
            cq.addAll(tempSet);
            // Flush the temporary set.
            tempSet.clear();
        }
        return ret;
    }
}
