package com.sun.labs.util.props;

import java.util.Map;

/**
 *
 */
public class MapConfigurable implements Configurable {

    @Config(genericType=java.lang.String.class)
    public Map<String,String> map;

}
