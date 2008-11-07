/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

/**
 * A standard implementation of the {@link Startable} interface, except for the 
 * run method.
 */
public abstract class StartableAdapter implements Startable {
    
    private Thread t;

    public void join() {
        try {
            t.join();
        } catch (InterruptedException ie) {
            
        }
    }

    public boolean isDone() {
        return !t.isAlive();
    }

    public void setThread(Thread t) {
        this.t = t;
    }

    public Thread getThread() {
        return t;
    }

}
