package com.oracle.labs.mlrg.olcut.config;

import java.util.logging.Logger;

/**
 * A basic configurable object that uses the Config annotation directly on the
 * types.
 */
public class BasicConfigurable implements Configurable {
    private static final Logger logger = Logger.getLogger(BasicConfigurable.class.getName());
    
    @Config
    public String s = "default";

    @Config
    public int i = 16;
    
    @Config
    public Integer bigI = 17;

    @Config
    public long l = 18;
    
    @Config
    public Long bigL = 19L;
    
    @Config
    public double d = 21;
    
    @Config
    public Double bigD = 22d;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicConfigurable that = (BasicConfigurable) o;

        if (i != that.i) return false;
        if (l != that.l) return false;
        if (Double.compare(that.d, d) != 0) return false;
        if (s != null ? !s.equals(that.s) : that.s != null) return false;
        if (bigI != null ? !bigI.equals(that.bigI) : that.bigI != null) return false;
        if (bigL != null ? !bigL.equals(that.bigL) : that.bigL != null) return false;
        return bigD != null ? bigD.equals(that.bigD) : that.bigD == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = s != null ? s.hashCode() : 0;
        result = 31 * result + i;
        result = 31 * result + (bigI != null ? bigI.hashCode() : 0);
        result = 31 * result + (int) (l ^ (l >>> 32));
        result = 31 * result + (bigL != null ? bigL.hashCode() : 0);
        temp = Double.doubleToLongBits(d);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (bigD != null ? bigD.hashCode() : 0);
        return result;
    }
}
