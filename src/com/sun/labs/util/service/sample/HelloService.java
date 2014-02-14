package com.sun.labs.util.service.sample;

import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A sample service.
 */
public interface HelloService extends Component, Remote {
    
    /**
     * Says hello
     * @return the string "hello"
     * @throws RemoteException 
     */
    public String hello() throws RemoteException;
    
    /**
     * Echoes the provided string.
     * @param s the string to echo
     * @return the same string
     * @throws RemoteException 
     */
    public String echo(String s) throws RemoteException;
}
