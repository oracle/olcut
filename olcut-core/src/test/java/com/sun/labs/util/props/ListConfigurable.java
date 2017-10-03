package com.sun.labs.util.props;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ListConfigurable implements Configurable {
    
    @Config(genericType=Configurable.class)
    List<Configurable> list = new ArrayList<>();
    
    public List<Configurable> getList() {
        return list;
    }

}
