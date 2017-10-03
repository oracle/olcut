package com.sun.labs.util.service.sample;

import com.sun.labs.util.props.Config;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.service.ConfigurableServiceAdapter;
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
