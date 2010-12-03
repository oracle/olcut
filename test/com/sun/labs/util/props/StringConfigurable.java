package com.sun.labs.util.props;

import java.util.logging.Logger;

/**
 * A configurable with a few strings.
 */
public class StringConfigurable implements Configurable {

    private Logger logger;
    
    @ConfigString(defaultValue="")
    public static final String PROP_ONE = "one";
    String one;

    @ConfigString(defaultValue="")
    public static final String PROP_TWO = "two";
    String two;
    
    @ConfigString(defaultValue="")
    public static final String PROP_THREE = "three";
    String three;

    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        one = ps.getString(PROP_ONE);
        two = ps.getString(PROP_TWO);
        three = ps.getString(PROP_THREE);
    }
}
