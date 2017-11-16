package com.oracle.labs.mlrg.olcut.service.sample;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.service.ConfigurableServiceAdapter;

import java.rmi.RemoteException;

/**
 * An implementation of the hello service that can run on a remote machine.
 */
public class HelloServiceImpl extends ConfigurableServiceAdapter implements HelloService, Configurable {
    
    @Config
    private String myString;

    @Override
    public String hello() throws RemoteException {
        return "Hello";
    }

    @Override
    public String echo(String s) throws RemoteException {
        return s;
    }

    @Override
    public double add(double d1, double d2) {
        return d1 + d2;
    }

    @Override
    public String getServiceName() {
        return "HelloService";
    }

}
