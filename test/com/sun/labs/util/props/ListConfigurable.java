package com.sun.labs.util.props;

import java.util.List;

/**
 *
 */
public class ListConfigurable implements Configurable {
    
    @ConfigComponentList(type=com.sun.labs.util.props.Configurable.class,
    defaultList={})
    public static final String PROP_LIST = "list";
    
    List<Configurable> list;
    
    public void newProperties(PropertySheet ps) {
        list = (List<Configurable>) ps.getComponentList(PROP_LIST);
    }
    
    public List<Configurable> getList() {
        return list;
    }

}
