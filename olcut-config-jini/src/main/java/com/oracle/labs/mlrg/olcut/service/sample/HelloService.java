package com.oracle.labs.mlrg.olcut.service.sample;

import com.oracle.labs.mlrg.olcut.config.Configurable;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A sample service.
 */
public interface HelloService extends Configurable, Remote {
    
    /**
     * Says hello
     * @return the string "hello"
     * @throws RemoteException If an error occurred in the remote JVM.
     */
    public String hello() throws RemoteException;
    
    /**
     * Echoes the provided string.
     * @param s the string to echo
     * @return the same string
     * @throws RemoteException If an error occurred in the remote JVM.
     */
    public String echo(String s) throws RemoteException;
    
    /**
     * Adds two doubles together in a very friendly way.
     * 
     * @param d1 the first double
     * @param d2 the second double
     * @return d1 + d2
     * @throws RemoteException If an error occurred in the remote JVM.
     */
    public double add(double d1, double d2) throws RemoteException;
}
