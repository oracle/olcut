package com.sun.labs.util.props;

/**
 * An interface for things that should be started in their own thread when they
 * are created.  Configurable components that also implement this interface will
 * be started in their own thread after they are instantiated and configured.
 */
public interface Startable extends Runnable {
    
    /**
     * Waits until the thread this object is running in is finished before returning.
     */
    public void join();
    
    /**
     * Checks whether the component is done with its work.
     * @return <code>true</code> if the component is finished its work
     */
    public boolean isDone();
    
    /**
     * Sets the thread that this object is running in.
     * 
     * @param t the thread that this object is running in
     */
    public void setThread(Thread t);

    /**
     * Gets the thread that this object is running in.
     * 
     * @return the thread that this object is running in
     */
    public Thread getThread();

}
