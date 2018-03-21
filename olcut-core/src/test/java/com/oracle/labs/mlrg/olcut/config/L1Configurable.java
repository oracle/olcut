package com.oracle.labs.mlrg.olcut.config;


import java.util.logging.Logger;

/**
 * Level one of a multi-level configurable chain, used to test importing.
 */
public class L1Configurable implements Configurable {

    private static final Logger logger = Logger.getLogger(L1Configurable.class.getName());

    @Config
    public String s = "l1";

    @Config
    public L2Configurable c;

}
