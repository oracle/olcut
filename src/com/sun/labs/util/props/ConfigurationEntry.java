package com.sun.labs.util.props;

import net.jini.entry.AbstractEntry;

/**
 * A configurable entry type that we can pass to a service registrar to 
 * enable service matching.
 */
public class ConfigurationEntry extends AbstractEntry implements Configurable {
    
    public ConfigurationEntry() {
        
    }
    
    public ConfigurationEntry(String data) {
        this.data = data;
    }
    
    public String data;

    public void newProperties(PropertySheet ps) throws PropertyException {
        data = ps.getString(PROP_DATA);
    }
    
    @ConfigString(defaultValue="")
    public static final String PROP_DATA = "data";

}
