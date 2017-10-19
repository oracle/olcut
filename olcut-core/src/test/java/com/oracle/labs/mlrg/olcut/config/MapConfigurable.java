package com.oracle.labs.mlrg.olcut.config;

import java.util.Map;

/**
 *
 */
public class MapConfigurable implements Configurable {

    @Config
    public Map<String,String> map;

}
