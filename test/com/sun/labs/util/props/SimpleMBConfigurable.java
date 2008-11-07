/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SimpleMBConfigurable implements Configurable, ConfigurableMXBean {
    @ConfigInteger(defaultValue=1)
    public static final String PROP_A = "a";

    private int a;

    @ConfigString(defaultValue="hello")
    public static final String PROP_B = "b";

    private String b;

    @ConfigStringList(defaultList={"foo", "bar"})
    public static final String PROP_C = "c";

    List<String> c;

    public void newProperties(PropertySheet ps) throws PropertyException {
        a = ps.getInt(PROP_A);
        b = ps.getString(PROP_B);
        c = ps.getStringList(PROP_C);
    }

    public String[] getProperties() {
        return new String[] {"a", "b", "c"};
    }

    public String getValue(String property) {
        if(property.equals(PROP_A)) {
            return String.valueOf(a);
        } else if(property.equals(PROP_B)) {
            return b;
        } else {
            return null;
        }
    }

    public String[] getValues(String property) {
        if(property.equals(PROP_C)) {
            return c.toArray(new String[0]);
        }
        return null;
    }

    public boolean setValue(String property, String value) {
        if(property.equals(PROP_A)) {
            try {
                a = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        } else if(property.equals(PROP_B)) {
            b = value;
            return true;
        } else {
            return false;
        }
    }

    public boolean setValues(String property,
            String[] values) {
        c = new ArrayList<String>();
        for(String value : values) {
            c.add(value);
        }
        return true;
    }

}
