package com.oracle.labs.mlrg.olcut.config;

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
