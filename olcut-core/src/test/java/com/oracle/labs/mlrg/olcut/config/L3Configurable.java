package com.oracle.labs.mlrg.olcut.config;


import java.util.logging.Logger;

/**
 * Level one of a multi-level configurable chain, used to test importing.
 */
public class L3Configurable implements Configurable {

    private static final Logger logger = Logger.getLogger(L3Configurable.class.getName());

    @Config
    public String s = "l3";

    @Config
    public BasicConfigurable c;

}
