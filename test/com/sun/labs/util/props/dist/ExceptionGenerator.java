package com.sun.labs.util.props.dist;

import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the interface for a remote component.  This component
 * will generate a remote exception every three operations.
 */
public class ExceptionGenerator implements RegistryConfigurable, Configurable {
    
    @ConfigString(defaultValue="prefix")
    public static final String PROP_PREFIX = "prefix";
    private String prefix;
    
    @ConfigInteger(defaultValue=1)
    public static final String PROP_INCR = "incr";
    private int incr;
    
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

    public void newProperties(PropertySheet ps) throws PropertyException {
        prefix = ps.getString(PROP_PREFIX);
        incr = ps.getInt(PROP_INCR);
    }
}
