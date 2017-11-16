package com.oracle.labs.mlrg.olcut.config.remote;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;

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
