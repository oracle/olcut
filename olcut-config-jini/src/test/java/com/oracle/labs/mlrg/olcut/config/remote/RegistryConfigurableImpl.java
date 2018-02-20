package com.oracle.labs.mlrg.olcut.config.remote;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the interface for a remote component.
 */
public class RegistryConfigurableImpl implements RegistryConfigurable, Configurable {
    
    @Config
    private String prefix = "prefix";
    
    @Config
    private int incr = 1;
    
    public List<String> recs = new ArrayList<String>();
    
    private int sopCount = 0;
    
    private int iopCount = 0;
    
    public int getIOPCount() {
        return iopCount;
    }

    public String stringOp(String s) throws RemoteException {
        recs.add(s);
        sopCount++;
        return prefix + s;
    }

    public int intOp(int x) throws RemoteException {
        iopCount++;
        return x + incr;
    }

}
