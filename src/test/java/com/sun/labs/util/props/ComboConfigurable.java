package com.sun.labs.util.props;


import java.util.logging.Logger;

/**
 * A simple configurable class with an embedded component.
 */
public class ComboConfigurable implements Configurable {

    private Logger logger;

    @ConfigString(defaultValue="alpha")
    public static final String PROP_ALPHA = "alpha";

    String alpha;

    @ConfigComponent(type=com.sun.labs.util.props.StringConfigurable.class)
    public static final String PROP_SC = "sc";

    StringConfigurable sc;

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        alpha = ps.getString(PROP_ALPHA);
        sc = (StringConfigurable) ps.getComponent(PROP_SC);
    }
}
