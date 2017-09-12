package com.sun.labs.util.props;


import java.util.logging.Logger;

/**
 * Level one of a multi-level configurable chain, used to test importing.
 */
public class L1Configurable implements Configurable {

    private static final Logger logger = Logger.getLogger(L1Configurable.class.getName());

    @Config
    String s = "l1";

    @Config
    L2Configurable c;

}
