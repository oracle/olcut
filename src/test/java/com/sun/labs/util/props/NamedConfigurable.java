package com.sun.labs.util.props;

/**
 *
 */
public class NamedConfigurable implements Configurable {

    @ConfigurableName
    private String name;

    private NamedConfigurable() { }

    public NamedConfigurable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return true;
        /*
        NamedConfigurable that = (NamedConfigurable) o;

        return name != null ? name.equals(that.name) : that.name == null;
        */
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NamedConfigurable(name="+name+")";
    }
}
