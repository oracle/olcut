package com.sun.labs.util.props;

/**
 * Created by johsulli on 5/11/17.
 */
public class FooConfigurable implements Configurable {

    @Config
    public String name;

    @Config int value;

    public FooConfigurable(String name, int value) {
        this.name = name;
        this.value = value;
    }

    private FooConfigurable() { }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FooConfigurable that = (FooConfigurable) o;

        if (value != that.value) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + value;
        return result;
    }
}
