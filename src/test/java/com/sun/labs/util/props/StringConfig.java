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

    @Override
    public String toString() {
        return "StringConfig{" + "one=" + one + ", two=" + two + ", three=" + three + '}';
    }

}
