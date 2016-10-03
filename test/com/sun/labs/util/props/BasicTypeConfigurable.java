package com.sun.labs.util.props;

import java.util.logging.Logger;

/**
 * A basic configurable object that uses the Config annotation directly on the
 * types.
 */
public class BasicTypeConfigurable implements Configurable {
    private Logger logger;
    
    @Config
    String s = "default";

    @Config
    int i = 16;
    
    @Config
    Integer bigI = 17;

    @Config
    long l = 18;
    
    @Config
    Long bigL = 19L;
    
    @Config
    double d = 21;
    
    @Config
    Double bigD = 22d;

    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
    }
}
