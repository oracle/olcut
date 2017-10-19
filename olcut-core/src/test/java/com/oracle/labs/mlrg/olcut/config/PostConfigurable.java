package com.oracle.labs.mlrg.olcut.config;

/**
 *
 * @author apocock
 */
public class PostConfigurable implements Configurable {

    @Config
    public String one;

    @Config
    public String two;
    
    @Override
    public void postConfig() {
        two = "Gorillas";
    }
    
}
