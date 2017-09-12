package com.sun.labs.util.props;

/**
 * A configurable array of configuration entries that can be passed to a
 * service registrar for registration or matching of a service.  The name of an array
 * of entries can be specified in the entries attribute of the component tag 
 * in a configuration file.
 */
public class ConfigurationEntries implements Configurable {

    @Config
    ConfigurationEntry[] entries;

    public ConfigurationEntry[] getEntries() {
        return entries;
    }

}
