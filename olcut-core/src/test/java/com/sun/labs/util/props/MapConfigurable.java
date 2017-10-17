package com.sun.labs.util.props;

import java.util.Map;

/**
 *
 */
public class MapConfigurable implements Configurable {

    @Config
    public Map<String,String> map;

}
