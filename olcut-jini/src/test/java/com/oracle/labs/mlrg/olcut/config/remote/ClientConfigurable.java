package com.oracle.labs.mlrg.olcut.config.remote;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * A client class that gets a configurable object from a registrar.
 */
public class ClientConfigurable implements Configurable, Runnable {
    private static final Logger log = Logger.getLogger(ClientConfigurable.class.getName());

    @Config
    private String value = "foo";

    @Config
    private int count = 10;

    @Config
    private RegistryConfigurable comp;

    private int newPropsCalls = 0;

    private int opCount = 0;

    private boolean pause = false;

    public void run() {
        for(int i = 0; i < count; i++) {
            try {
                comp.stringOp(value);
                comp.intOp(count);
                opCount++;
                log.info(Thread.currentThread().getName() + " iteration " + i);
            } catch(RemoteException rx) {
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

    @Override
    public void postConfig() throws PropertyException {
        newPropsCalls++;
        pause = false;
    }
}
