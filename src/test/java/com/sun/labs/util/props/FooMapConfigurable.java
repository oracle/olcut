package com.sun.labs.util.props;

import java.util.Map;

/**
 * Created by johsulli on 5/11/17.
 */
public class FooMapConfigurable implements Configurable {

    @Config
    public Map<String, FooConfigurable> map;
}
