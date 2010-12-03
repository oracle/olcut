package com.sun.labs.util.props;


import java.util.logging.Logger;

/**
 * Level one of a multi-level configurable chain, used to test importing.
 */
public class L3Configurable implements Configurable {

    private Logger logger;

    @ConfigString(defaultValue="l3")
    public static final String PROP_S = "s";

    String s;

    @ConfigComponent(type=com.sun.labs.util.props.BasicConfigurable.class)
    public static final String PROP_C = "c";

    BasicConfigurable c;

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        s = ps.getString(PROP_S);
        c = (BasicConfigurable) ps.getComponent(PROP_C);
    }

}
