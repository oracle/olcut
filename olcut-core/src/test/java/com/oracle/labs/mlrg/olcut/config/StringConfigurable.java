package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.provenance.ConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenancable;
import com.oracle.labs.mlrg.olcut.provenance.impl.ConfiguredObjectProvenanceImpl;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * A configurable with a few strings. Uses the @Config annotation to directly
 * annotate configurable members.
 */
public class StringConfigurable implements Configurable, Provenancable<ConfiguredObjectProvenance>, Serializable {

    protected Logger logger;

    @Config
    public String one = "";

    @Config
    public String two = "";

    @Config
    public String three = "";

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

    @Override
    public ConfiguredObjectProvenance getProvenance() {
        return new ConfiguredObjectProvenanceImpl(this,"StringConfigurable");
    }
}
