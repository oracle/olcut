package com.sun.labs.util.service.sample;

import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import com.sun.labs.util.service.ConfigurableServiceAdapter;
import java.rmi.RemoteException;

/**
 * An implementation of the hello service that can run on a remote machine.
 */
public class HelloServiceImpl extends ConfigurableServiceAdapter implements HelloService, Configurable {
    
    @ConfigString
    public static final String PROP_MY_STRING = "myString";
    
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
    public void newProperties(PropertySheet ps) throws PropertyException {
        myString = ps.getString(PROP_MY_STRING);
    }

    @Override
    public String getServiceName() {
        return "HelloService";
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
