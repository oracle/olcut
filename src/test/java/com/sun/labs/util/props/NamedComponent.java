package com.sun.labs.util.props;

/**
 *
 */
public class NamedComponent implements Component {

    @ComponentName
    private String name;

    private NamedComponent() { }

    public NamedComponent(String name) {
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
        NamedComponent that = (NamedComponent) o;

        return name != null ? name.equals(that.name) : that.name == null;
        */
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NamedComponent(name="+name+")";
    }
}