package com.sun.labs.util.props;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
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

    public static String getUsage(Class<? extends Options> options) {
        Set<Field> fields = getOptionFields(options);
        if (fields.size() == 0) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();

            builder.append(options.getSimpleName());
            builder.append('\n');

            builder.append("Char\t\tLong Name\t\tUsage\n");
            for (Field f : fields) {
                Option option = f.getAnnotation(Option.class);
                builder.append(Options.getOptionUsage(option));
            }
            builder.append('\n');

            return builder.toString();
        }
    }

    public static String getOptionUsage(Option option) {
        StringBuilder builder = new StringBuilder();
        if (option.charName() != Option.EMPTY_CHAR) {
            builder.append(option.charName());
        } else {
            builder.append(' ');
        }
        builder.append("\t\t");
        builder.append(option.longName());
        builder.append("\t\t");
        builder.append(option.usage());
        builder.append('\n');
        return builder.toString();
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
