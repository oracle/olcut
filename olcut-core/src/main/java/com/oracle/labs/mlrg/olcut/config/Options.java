package com.oracle.labs.mlrg.olcut.config;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

/**
 * A tag interface which contains fields annotated with {@link Option}, and other
 * fields which subclass {@link Options}. Any other fields or methods are ignored by the
 * command line arguments processing system.
 */
public interface Options {

    public static final ArrayList<String> header = new ArrayList<>(Arrays.asList("Char","Long Name","Type","Usage"));

    public static String formatUsage(ArrayList<ArrayList<String>> usageList) {
        int[] maxWidth = new int[4];

        for (ArrayList<String> a : usageList) {
            if (a.size() == 4) {
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

        String formatString = "%"+maxWidth[0]+"s %-"+maxWidth[1]+"s %-"+maxWidth[2]+"s %-"+maxWidth[3]+"s\n";
        StringBuilder builder = new StringBuilder();

        for (ArrayList<String> a : usageList) {
            if (a.size() == 4) {
                builder.append(String.format(formatString,a.get(0),a.get(1),a.get(2),a.get(3)));
            } else {
                // Must be Option class name
                builder.append("\n");
                builder.append(a.get(0));
                builder.append("\n\n");
            }
        }

        return builder.toString();
    }

    public static ArrayList<ArrayList<String>> getUsage(Class<? extends Options> options) {
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        Set<Field> fields = getOptionFields(options);
        if (fields.size() == 0) {
            return list;
        } else {
            list.add(new ArrayList<>(Arrays.asList(options.getSimpleName())));

            list.add(header);
            for (Field f : fields) {
                Option option = f.getAnnotation(Option.class);
                list.add(Options.getOptionUsage(option,f));
            }

            return list;
        }
    }

    public static ArrayList<String> getOptionUsage(Option option, Field f) {
        return getOptionUsage(option,f.getGenericType().getTypeName());
    }

    public static ArrayList<String> getOptionUsage(Option option, String type) {
        ArrayList<String> output = new ArrayList<>();
        if (option.charName() != Option.EMPTY_CHAR) {
            output.add(""+option.charName());
        } else {
            output.add(" ");
        }
        output.add(option.longName());
        output.add(type);
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
        return ret;
    }

    /**
     * Gets all of the fields which subclass {@link Options} by walking up the
     * class tree. Handles super classes, as well as interfaces.
     *
     * @param options the class who's fields we wish to walk.
     * @return all of the fields which subclass Options.
     */
    public static Set<Class<? extends Options>> getAllOptions(Class<? extends Options> options) {
        Set<Class<? extends Options>> ret = new LinkedHashSet<>();
        ret.add(options);
        Queue<Class> cq = new ArrayDeque<>();
        cq.add(options);
        while (!cq.isEmpty()) {
            Class curr = cq.remove();
            for (Field f : curr.getDeclaredFields()) {
                if (Options.class.isAssignableFrom(f.getType())) {
                    ret.add((Class<? extends Options>)f.getType());
                }
            }
            for (Field f : curr.getFields()) {
                if (Options.class.isAssignableFrom(f.getType())) {
                    ret.add((Class<? extends Options>)f.getType());
                }
            }
            Class sc = curr.getSuperclass();
            if (sc != null) {
                cq.add(sc);
            }
            cq.addAll(Arrays.asList(curr.getInterfaces()));
        }
        return ret;
    }
}
