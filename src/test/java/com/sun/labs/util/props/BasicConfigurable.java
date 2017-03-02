package com.sun.labs.util.props;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * A configurable with a few strings.
 */
public class BasicConfigurable implements Configurable, Serializable {

    private Logger logger;
    
    @ConfigString(defaultValue="")
    public static final String PROP_S = "s";
    String s;

    @ConfigInteger(defaultValue=0)
    public static final String PROP_I = "i";
    int i;

    @ConfigDouble(defaultValue=0)
    public static final String PROP_D = "d";
    double d;

    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        s = ps.getString(PROP_S);
        i = ps.getInt(PROP_I);
        d = ps.getDouble(PROP_D);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final BasicConfigurable other = (BasicConfigurable) obj;
        if((this.s == null) ? (other.s != null) : !this.s.equals(other.s)) {
            return false;
        }
        if(this.i != other.i) {
            return false;
        }
        if(Double.doubleToLongBits(this.d) != Double.doubleToLongBits(other.d)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.s != null ? this.s.hashCode() : 0);
        hash = 41 * hash + this.i;
        hash =
                41 * hash +
                (int) (Double.doubleToLongBits(this.d) ^
                (Double.doubleToLongBits(this.d) >>> 32));
        return hash;
    }
    
    
}
