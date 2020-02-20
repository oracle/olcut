package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;

import java.util.List;

/**
 *
 */
public class InvalidListConfigurable implements Configurable {

    // SimpleProperty does not implement Configurable, this class is invalid.
    @Config
    public List<SimpleProperty> list;

}
