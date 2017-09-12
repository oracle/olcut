package com.sun.labs.util.props;

import java.util.ArrayList;
import java.util.List;

/**
 * A configurable that takes a list of strings.
 */
public class StringListConfigurable implements Configurable {

    @Config(genericType=String.class)
    public List<String> strings = new ArrayList<String>();

}
