package com.oracle.labs.mlrg.olcut.config;

import java.util.ArrayList;
import java.util.List;

/**
 * A configurable that takes a list of strings.
 */
public class StringListConfigurable implements Configurable {

    @Config
    public List<String> strings = new ArrayList<String>();

}
