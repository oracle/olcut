package com.sun.labs.util.props;

import java.util.Set;

/**
 *
 */
public class SetConfig implements Configurable {

    @Config(genericType=java.lang.String.class)
    public Set<String> stringSet;

    @Config(genericType=java.lang.Double.class)
    public Set<Double> doubleSet;

    @Config(genericType=StringConfig.class)
    public Set<StringConfig> stringConfigSet;

}
