package com.sun.labs.util.props;

/**
 * A sub-class of StringConfigurable that we can use to test inheritance.
 */
public class StringleConfigurable extends StringConfigurable {
    @ConfigString(defaultValue="")
    public static final String PROP_FOUR = "four";
    public String four;
    
    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        super.newProperties(ps);
        four = ps.getString(PROP_FOUR);
    }
}
