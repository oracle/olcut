package com.oracle.labs.mlrg.olcut.config;


import java.util.logging.Logger;

/**
 * Level two of a multi-level configurable chain, used to test importing.
 */
public class L2Configurable implements Configurable {

    private static final Logger logger = Logger.getLogger(L2Configurable.class.getName());

    @Config
    public String s = "l2";

    @Config
    public L3Configurable c;

}
