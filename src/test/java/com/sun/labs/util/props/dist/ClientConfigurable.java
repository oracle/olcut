package com.sun.labs.util.props.dist;

import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * A client class that gets a configurable object from a registrar.
 */
public class ClientConfigurable implements Configurable, Runnable {

    @ConfigString(defaultValue = "foo")
    public static final String PROP_VALUE = "value";

    private String value;

    @ConfigInteger(defaultValue = 10)
    public static final String PROP_COUNT = "count";

    private int count;

    @ConfigComponent(type = com.sun.labs.util.props.dist.RegistryConfigurable.class)
    public static final String PROP_COMP = "comp";

    private RegistryConfigurable comp;

    private Logger log;

    private int newPropsCalls;

    private int opCount;

    private boolean pause;

    public void run() {
        for(int i = 0; i < count; i++) {
            try {
                comp.stringOp(value);
                comp.intOp(count);
                opCount++;
                log.info(Thread.currentThread().getName() + " iteration " + i);
            } catch(RemoteException rx) {
                pause = true;
                while(pause) {
                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException ie) {

                    }
                }
            }
            try {
                Thread.sleep(500);
            } catch(InterruptedException ie) {

            }


        }
        log.info("Client done!");
    }

    public int getNewPropsCalls() {
        return newPropsCalls;
    }

    public int getOpCount() {
        return opCount;
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        newPropsCalls++;
        log = ps.getLogger();
        value = ps.getString(PROP_VALUE);
        count = ps.getInt(PROP_COUNT);
        comp = (RegistryConfigurable) ps.getComponent(PROP_COMP);
        pause = false;
    }
}
