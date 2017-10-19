/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oracle.labs.mlrg.olcut.config;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class StartableConfigurable extends StartableAdapter implements Configurable {
    @Config
    private String add = "Howdy!";

    @Config
    private int numReps = 10;
    
    private String cat;
    
    private List<String> result = new ArrayList<>();
    
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

}
