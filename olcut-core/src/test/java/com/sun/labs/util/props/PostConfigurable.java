package com.sun.labs.util.props;

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
