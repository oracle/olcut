/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class StartableConfigurable extends StartableAdapter implements Configurable {
    private String add;
    
    private int numReps;
    
    private String cat;
    
    private List<String> result;
    
    @ConfigInteger(defaultValue=10)
    public static final String PROP_NUM_REPS = "numReps";
    
    @ConfigString(defaultValue="Howdy!")
    public static final String PROP_ADD = "add";
    
    public List<String> getResult() {
        return result;
    }

    public void run() {
        try {
            Thread.sleep(3000);
            for(int i = 0; i < numReps;
                    i++) {
                result.add(add);
            }
        } catch(InterruptedException ex) {
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        result = new ArrayList<String>();
        numReps = ps.getInt(PROP_NUM_REPS);
        add = ps.getString(PROP_ADD);
    }

}
