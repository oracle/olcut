package com.sun.labs.util.props;


import java.util.logging.Logger;

/**
 * Level one of a multi-level configurable chain, used to test importing.
 */
public class L2Configurable implements Configurable {

    private Logger logger;

    @ConfigString(defaultValue="l2")
    public static final String PROP_S = "s";

    String s;

    @ConfigComponent(type=com.sun.labs.util.props.L3Configurable.class)
    public static final String PROP_C = "c";

    L3Configurable c;

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        s = ps.getString(PROP_S);
        c = (L3Configurable) ps.getComponent(PROP_C);
    }

}
