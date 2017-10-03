package com.sun.labs.util.props.dist;

import com.sun.labs.util.props.Config;
import com.sun.labs.util.props.Configurable;
import net.jini.entry.AbstractEntry;

/**
 * A configurable entry type that we can pass to a service registrar to 
 * enable service matching.
 */
public class ConfigurationEntry extends AbstractEntry implements Configurable {

    @Config
    public String data;

    public ConfigurationEntry() { }
    
    public ConfigurationEntry(String data) {
        this.data = data;
    }

}
