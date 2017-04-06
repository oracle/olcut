package com.sun.labs.util.props;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * A configurable with a few strings. Uses the @Config annotation to directly
 * annotate configurable members.
 */
public class StringConfig implements Configurable, Serializable {

    protected Logger logger;

    @Config
    String one = "";

    @Config
    String two = "";

    @Config
    String three = "";

    public StringConfig() {}

    public StringConfig(String one, String two, String three) {
        this.one = one;
        this.two = two;
        this.three = three;
    }

    @Override
    public String toString() {
        return "StringConfig{" + "one=" + one + ", two=" + two + ", three=" + three + '}';
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof StringConfig) {
            StringConfig sc = (StringConfig) other;
            return one.equals(sc.one) && two.equals(sc.two) && three.equals(sc.three);
        } else {
            return false;
        }
    }
}
