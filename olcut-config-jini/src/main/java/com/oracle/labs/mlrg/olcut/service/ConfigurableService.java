
package com.oracle.labs.mlrg.olcut.service;

import com.oracle.labs.mlrg.olcut.config.Configurable;

/**
 * An interface for starting and stopping configurable services.
 * 
 * @see ConfigurableServiceStarter
 */
public interface ConfigurableService extends Configurable, Runnable {
    /**
     * Gets the name of the service.
     * @return The service name.
     */
    public String getServiceName();

    /**
     * Tells the service about the starter that started it.
     * @param starter the starter
     */
    public void setStarter(ConfigurableServiceStarter starter);

    /**
     * Stops the service.
     */
    public void stop();
    
}
