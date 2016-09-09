package com.sun.labs.util.props;

import java.util.logging.Logger;

/**
 * A basic configurable object that uses the Config annotation directly on the
 * types.
 */
public class BasicTypeConfigurable implements Configurable {
    private Logger logger;
    
    @Config
    String s = "";

    @Config
    int i = 0;

    @Config
    double d = 0;

    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
    }
}
