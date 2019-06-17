package com.oracle.labs.mlrg.olcut.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ListProperty is a container for two lists, one of {@link SimpleProperty} instances and
 * one of {@link Class} instances. The Class instances are used to look up all instances of that class and
 * insert them into the field.
 */
public final class ListProperty implements Property {
    private static final Logger logger = Logger.getLogger(ListProperty.class.getName());

    private final List<SimpleProperty> simpleList;

    private final List<Class<?>> classList;

    public ListProperty(List<SimpleProperty> simpleList, List<Class<?>> classList) {
        this.simpleList = Collections.unmodifiableList(simpleList);
        this.classList = Collections.unmodifiableList(classList);
    }

    public ListProperty(List<SimpleProperty> simpleList) {
        this.simpleList = Collections.unmodifiableList(simpleList);
        this.classList = Collections.emptyList();
    }

    public List<SimpleProperty> getSimpleList() {
        return simpleList;
    }

    public List<Class<?>> getClassList() {
        return classList;
    }

    @Override
    public ListProperty copy() {
        ArrayList<SimpleProperty> newSimpleList = new ArrayList<>();
        for (SimpleProperty p : simpleList) {
            newSimpleList.add(p.copy());
        }
        if (classList.isEmpty()) {
            return new ListProperty(newSimpleList);
        } else {
            return new ListProperty(newSimpleList, new ArrayList<>(classList));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListProperty)) return false;
        ListProperty that = (ListProperty) o;
        return getSimpleList().equals(that.getSimpleList()) &&
                getClassList().equals(that.getClassList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSimpleList(), getClassList());
    }

    @Override
    public String toString() {
        //throw new IllegalStateException();
        return "[" + simpleList.toString() + ", " + classList.toString() + "]";
    }

    public static ListProperty createFromStringList(List<String> stringList) {
        List<SimpleProperty> output = new ArrayList<>();

        for (String s : stringList) {
            output.add(new SimpleProperty(s));
        }

        return new ListProperty(output);
    }
}
