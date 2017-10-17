package com.sun.labs.util.props;

import java.util.Map;

/**
 * Created by johsulli on 5/11/17.
 */
public class FooMapConfigurable implements Configurable {

    @Config
    public Map<String, FooConfigurable> map;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FooMapConfigurable that = (FooMapConfigurable) o;

        return map != null ? map.equals(that.map) : that.map == null;
    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }
}
