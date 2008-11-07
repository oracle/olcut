/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

import java.util.logging.Logger;

/**
 * A configurable with a few strings.
 */
public class StringConfig implements Configurable {
    
    @ConfigString(defaultValue="")
    public static final String PROP_ONE = "one";
    String one;
    @ConfigString(defaultValue="")
    public static final String PROP_TWO = "two";
    String two;
    @ConfigString(defaultValue="")
    public static final String PROP_THREE = "three";
    String three;

    public void newProperties(PropertySheet ps) throws PropertyException {
        one = ps.getString(PROP_ONE);
        two = ps.getString(PROP_TWO);
        three = ps.getString(PROP_THREE);
    }
}
