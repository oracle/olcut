package com.sun.labs.util.props;

import java.util.List;

/**
 *
 */
public class ListConfig implements Configurable {

    @Config(genericType=String.class)
    public List<String> stringList;

    @Config(genericType=Double.class)
    public List<Double> doubleList;

    @Config(genericType=StringConfig.class)
    public List<StringConfig> stringConfigList;

}
