package com.oracle.labs.mlrg.olcut.config;


import java.util.logging.Logger;

/**
 * Level two of a multi-level configurable chain, used to test importing.
 */
public class L2Configurable implements Configurable {

    private static final Logger logger = Logger.getLogger(L2Configurable.class.getName());

    @Config
    String s = "l2";

    @Config
    L3Configurable c;

}
