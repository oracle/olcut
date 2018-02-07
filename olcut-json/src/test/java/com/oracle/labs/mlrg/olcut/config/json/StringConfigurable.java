package com.oracle.labs.mlrg.olcut.config.json;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * A configurable with a few strings. Uses the @Config annotation to directly
 * annotate configurable members.
 */
public class StringConfigurable implements Configurable, Serializable {

    protected Logger logger;

    @Config
    String one = "";

    @Config
    String two = "";

    @Config
    String three = "";

    public StringConfigurable() {}

    public StringConfigurable(String one, String two, String three) {
        this.one = one;
        this.two = two;
        this.three = three;
    }

    @Override
    public String toString() {
        return "StringConfigurable{" + "one=" + one + ", two=" + two + ", three=" + three + '}';
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof StringConfigurable) {
            StringConfigurable sc = (StringConfigurable) other;
            return one.equals(sc.one) && two.equals(sc.two) && three.equals(sc.three);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = one.hashCode();
        result = 31 * result + two.hashCode();
        result = 31 * result + three.hashCode();
        return result;
    }
}
