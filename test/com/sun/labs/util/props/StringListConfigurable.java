package com.sun.labs.util.props;

import java.util.List;

/**
 * A configurable that takes a list of strings.
 */
public class StringListConfigurable implements Configurable {

    @ConfigStringList(defaultList = {})
    public static final String PROP_STRINGS = "strings";
    public List<String> strings;

    public void newProperties(PropertySheet ps) throws PropertyException {
        strings = ps.getStringList(PROP_STRINGS);
    }
}
