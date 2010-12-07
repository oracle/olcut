/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

import java.util.logging.Logger;

/**
 * A configurable with a few strings.
 */
public class BasicConfigurable implements Configurable {

    private Logger logger;
    
    @ConfigString(defaultValue="")
    public static final String PROP_S = "s";
    String s;

    @ConfigInteger(defaultValue=0)
    public static final String PROP_I = "i";
    int i;

    @ConfigDouble(defaultValue=0)
    public static final String PROP_D = "d";
    double d;

    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        s = ps.getString(PROP_S);
        i = ps.getInt(PROP_I);
        d = ps.getDouble(PROP_D);
    }
}
