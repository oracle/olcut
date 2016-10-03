package com.sun.labs.util.props;

/**
 *
 */
public class ListTypeConfigurable implements Configurable {
    
    @Config
    Configurable[] list = null;
    
    public Configurable[] getList() {
        return list;
    }
}
