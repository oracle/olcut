/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oracle.labs.mlrg.olcut.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class SimpleMBConfigurable implements Configurable, ConfigurableMXBean {
    @Config
    private int a = 1;

    @Config
    private String b = "hello";

    @Config
    List<String> c = Arrays.asList("foo","bar");

    public String[] getProperties() {
        return new String[] {"a", "b", "c"};
    }

    public String getValue(String property) {
        if(property.equals("a")) {
            return String.valueOf(a);
        } else if(property.equals("b")) {
            return b;
        } else {
            return null;
        }
    }

    public String[] getValues(String property) {
        if(property.equals("c")) {
            return c.toArray(new String[0]);
        }
        return null;
    }

    public boolean setValue(String property, String value) {
        if(property.equals("a")) {
            try {
                a = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        } else if(property.equals("b")) {
            b = value;
            return true;
        } else {
            return false;
        }
    }

    public boolean setValues(String property, String[] values) {
        c = new ArrayList<String>();
        for(String value : values) {
            c.add(value);
        }
        return true;
    }

}
