package com.sun.labs.util.props.dist;

import com.sun.labs.util.props.Config;
import com.sun.labs.util.props.ConMan;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;

import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * A client class that gets a configurable object from a registrar.
 */
public class ClientReconfigurable implements Configurable, Runnable {
    private static final Logger log = Logger.getLogger(ClientReconfigurable.class.getName());

    @Config
    private String value = "foo";

    @Config
    private int count = 10;

    @Config
    private RegistryConfigurable comp;

    private int newPropsCalls;
    
    private int opCount;
    
    private int exCount;

    @ConMan
    private ConfigurationManager cm;

    public void run() {
        for(int i = 0; i < count; i++) {
            try {
                comp.stringOp(value);
                comp.intOp(count);
                opCount++;
            } catch(RemoteException rx) {
                exCount++;
                log.info("Got exception " + exCount);
                //
                // Reconfigure when we have trouble, and re-try this iteration.
                cm.reconfigure(this);
                i--;
                log.info("Reconfigured");
            }
        }
    }
    
    public int getNewPropsCalls() {
        return newPropsCalls;
    }
    
    public int getOpCount() {
        return opCount;
    }
    
    public int getExCount() {
        return exCount;
    }

    @Override
    public void postConfig() throws PropertyException {
        newPropsCalls++;
    }
}
