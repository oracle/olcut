package com.oracle.labs.mlrg.olcut.config;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ListConfigurable implements Configurable {
    
    @Config
    public List<Configurable> list = new ArrayList<>();
    
    public List<Configurable> getList() {
        return list;
    }

}
