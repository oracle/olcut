package com.sun.labs.util.props;

import java.util.List;

/**
 * A configurable list of configuration entries that can be passed to a 
 * service registrar for registration or matching of a service.  The name of a list
 * of entries can be specified in the entries attribute of the component tag 
 * in a configuration file.
 */
public class ConfigurationEntries implements Configurable {
    
    ConfigurationEntry[] entries;

    public void newProperties(PropertySheet ps) throws PropertyException {
        List<String> temp = ps.getStringList(PROP_ENTRIES);
        if(temp.size() > 0) {
            entries = new ConfigurationEntry[temp.size()];
            for(int i = 0; i < temp.size(); i++) {
                entries[i] = new ConfigurationEntry(temp.get(i));
            }
        }
    }
    
    public ConfigurationEntry[] getEntries() {
        return entries;
    }
    
    @ConfigStringList(defaultList={})
    public static final String PROP_ENTRIES = "entries";

}
