package com.sun.labs.util.props.dist;

import com.sun.labs.util.props.Config;
import com.sun.labs.util.props.Configurable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the interface for a remote component.  This component
 * will generate a remote exception every three operations.
 */
public class ExceptionGenerator implements RegistryConfigurable, Configurable {
    
    @Config
    private String prefix = "prefix";
    
    @Config
    private int incr = 1;
    
    public List<String> recs = new ArrayList<String>();
    
    private int sopCount = 0;
    
    private int iopCount = 0;
    
    private int exCount = 0;
    
    private int rawCount = 0;
    
    public int getIOPCount() {
        return iopCount;
    }
    
    public int getExCount() {
        return exCount;
    }

    public String stringOp(String s) throws RemoteException {
        rawCount++;
        if(rawCount % 3 == 2) {
            exCount++;
            throw new RemoteException("It's an exception!");
        }
        recs.add(s);
        sopCount++;
        return prefix + s;
    }

    public int intOp(int x) throws RemoteException {
        iopCount++;
        return x + incr;
    }
}
