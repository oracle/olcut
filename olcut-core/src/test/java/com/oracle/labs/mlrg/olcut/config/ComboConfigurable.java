package com.oracle.labs.mlrg.olcut.config;

import java.util.logging.Logger;

/**
 * A simple configurable class with an embedded component.
 */
public class ComboConfigurable implements Configurable {

    private static final Logger logger = Logger.getLogger(ComboConfigurable.class.getName());

    @Config
    public String alpha = "alpha";

    @Config
    public StringConfigurable sc;

}
