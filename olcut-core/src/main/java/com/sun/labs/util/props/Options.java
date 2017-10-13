package com.sun.labs.util.props;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

public interface Options {

    public String getName();

    public static String getUsage(Class<? extends Options> options) {
        Set<Field> fields = getOptionFields(options);
        StringBuilder builder = new StringBuilder();

        try {
            Options o = options.newInstance();
            builder.append(o.getName());
            builder.append('\n');
        } catch (IllegalAccessException | InstantiationException e) {
            throw new ArgumentException(e,"Failed to construct options of type " + options.getName());
        }

        builder.append("Char\t\tLong Name\t\tUsage\n");
        for (Field f : fields) {
            Option option = f.getAnnotation(Option.class);
            if (option.charName() != '\0') {
                builder.append(option.charName());
            } else {
                builder.append(' ');
            }
            builder.append("\t\t");
            builder.append(option.longName());
            builder.append("\t\t");
            builder.append(option.usage());
            builder.append('\n');
        }

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
     * Gets all of the fields which subclass {@link Options} by walking up the
     * class tree. Handles super classes, as well as interfaces.
     *
     * @param options the class who's fields we wish to walk.
     * @return all of the fields which subclass Options.
     */
    public static Set<Class<? extends Options>> getAllOptions(Class<? extends Options> options) {
        Set<Class<? extends Options>> ret = new LinkedHashSet<>();
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
